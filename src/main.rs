
fn main() {
	parse(Path::new("./testToolpaths/f1/f1_Kuka_APT2_Main.SRC"));
}

struct Toolpath {
	name: String,
	head: Vec<String>,
	body: Vec<String>
}

use std::fs;
use std::fs::{File, DirBuilder};
use std::path::Path;
use std::io::{BufReader, BufRead, Write};

fn parse(main_toolpath: &Path) {
	let parent = match main_toolpath.parent() {
		Some(p) => p,
		None => panic!("Error getting parent")
	};

	let files = match fs::read_dir(parent) {
		Ok(sf) => sf,
		Err(..) => panic!("Error getting files in directory")
	};

	let mut source_files: Vec<String> = Vec::new();
	for path in files {
		let entry = path.unwrap();
		let name = entry.path();
		let file = name.to_str().unwrap();
		if file.ends_with(".SRC") { source_files.push(file.to_string()); }
	}

	source_files.sort();
	let _ = source_files.pop();

    let mut toolpaths: Vec<Toolpath> = Vec::new();
    get_toolpaths(&source_files, &mut toolpaths);
    let num_toolpaths = &toolpaths.len();
    println!("{}", &num_toolpaths);

    let group_nums = ((*num_toolpaths as f32) / (8 as f32)).ceil() as usize;
    println!("{}", &group_nums);
    let mut groups = Vec::new();
    for _ in 0..group_nums {
        if toolpaths.len() >= 8 {
            groups.push(toolpaths.drain(0..8).collect::<Vec<Toolpath>>());
        } else {
            groups.push(toolpaths.drain(0..).collect::<Vec<Toolpath>>());;
        }
    }

    for group in &mut groups {
        let mut last = group.last_mut().unwrap();
        last.body.pop();
        last.body.append(&mut vec!["SetRPM(0)".to_string()]);
        last.body.append(&mut vec!["End".to_string()]);
    }

    let mut main_source: Vec<String> = Vec::new();
    get_main_source(main_toolpath, &mut main_source);
    write_groups(main_toolpath, &main_source, &groups, &num_toolpaths);

}

fn get_toolpaths(source_files: &Vec<String>, toolpaths: &mut Vec<Toolpath>) {
    for file_name in source_files {
        let file = File::open(&file_name).unwrap();
        let reader = BufReader::new(file);
        let mut lines = reader.lines();

        let mut head: Vec<String> = Vec::new();
        let mut body: Vec<String> = Vec::new();

        while let Some(l) = lines.next() {
            let line = l.unwrap();
            if !line.contains("SetRPM") {
                head.push(line);
            } else {
                body.push(line);
                break;
            }
        }

        while let Some(l) = lines.next() {
            let line = l.unwrap();
            body.push(line);
        }
        head.retain(|l| l != "");
        head.retain(|l| l != " ");
        body.retain(|l| l != "");
        body.retain(|l| l != " ");
        let name = Path::new(file_name).file_name().unwrap().to_str().unwrap().to_string();
        let toolpath = Toolpath { name: name, head: head, body: body };
        toolpaths.push(toolpath);
    }
}

fn get_main_source(main_toolpath: &Path, main_source: &mut Vec<String>) {
    let file = File::open(main_toolpath).unwrap();
    let reader = BufReader::new(file);
    let mut lines = reader.lines();

    while let Some(l) = lines.next() {
        let line = l.unwrap();
        if line != "" && line != " " { main_source.push(line); }
    }
}

fn write_groups(main_toolpath: &Path, main_source: &Vec<String>, groups: &Vec<Vec<Toolpath>>, num_toolpaths: &usize) {
    let mut i = 1;
    let mut first_toolpath: Vec<_> = groups[0][0].name.split('/').collect();
    let first_toolpath: &str = first_toolpath.pop().unwrap();
    let first_toolpath: Vec<_> = first_toolpath.split('.').collect();
    let first_toolpath: &str = first_toolpath[0];
    let first_toolpath: String = first_toolpath.to_string();
    for group in groups {
        let dir = format!("{}{}{}", main_toolpath.parent().unwrap().display(), "/Toolpath_", &i);
        let dir = Path::new(&dir);
        if !fs::metadata(&dir).is_ok() { let _ = DirBuilder::new().recursive(true).create(&dir); }

        for toolpath in group {
            let file = Path::new(&dir).join(&toolpath.name);
            let mut file = File::create(file).unwrap();
            for line in &toolpath.head { file.write(line.as_bytes()).unwrap(); file.write("\n".as_bytes()).unwrap(); }
            for line in &toolpath.body { file.write(line.as_bytes()).unwrap(); file.write("\n".as_bytes()).unwrap(); }
        }
        let main_dir = &dir.join(main_toolpath.file_name().unwrap().to_str().unwrap());
        write_main(&main_dir, main_source, &group, &first_toolpath, num_toolpaths);
        i = i + 1;
    }
}

fn write_main(dir: &Path, main_source: &Vec<String>, group: &Vec<Toolpath>, first_toolpath: &String, num_toolpaths: &usize) {

    let mut head: Vec<String> = Vec::new();
    let mut middle: Vec<String> = Vec::new();
    let mut bottom: Vec<String> = Vec::new();

    let mut main_source = main_source.iter();
    while let Some(line) = main_source.next() {
        if !line.as_str().contains(first_toolpath) {
            head.push(line.to_string());
        } else {
            middle.push(line.to_string());
            break;
        }
    }

    while let Some(line) = main_source.next() {
        if !line.as_str().contains(first_toolpath) {
            middle.push(line.to_string());
        } else {
            bottom.push(line.to_string());
            break;
        }
    }

    while let Some(line) = main_source.next() { bottom.push(line.to_string()); }

    head.retain(|l| !l.as_str().contains(first_toolpath));
    head.retain(|l| l != "");
    head.retain(|l| l != " ");

    for toolpath in group {
        let mut name: Vec<_> = toolpath.name.split('/').collect();
        let name: &str = name.pop().unwrap();
        let name: Vec<_> = name.split('.').collect();
        let name: &str = name[0];
        let name: String = name.to_string();
        let name = format!("EXT {}()", name);
        head.push(name);
    }

    middle.drain(0..*num_toolpaths);
    middle.retain(|l| l != "");
    middle.retain(|l| l != " ");

    for toolpath in group {
        let mut name: Vec<_> = toolpath.name.split('/').collect();
        let name: &str = name.pop().unwrap();
        let name: Vec<_> = name.split('.').collect();
        let name: &str = name[0];
        let name: String = name.to_string();
        let name = format!("{}()", name);
        middle.push(name);
    }

    bottom.drain(0..*num_toolpaths);
    bottom.retain(|l| l != "");
    bottom.retain(|l| l != " ");

    let mut file = File::create(dir).unwrap();
    for l in head { file.write(l.as_bytes()).unwrap(); file.write("\n".as_bytes()).unwrap(); }
    for l in middle { file.write(l.as_bytes()).unwrap(); file.write("\n".as_bytes()).unwrap(); }
    for l in bottom { file.write(l.as_bytes()).unwrap(); file.write("\n".as_bytes()).unwrap(); }
}