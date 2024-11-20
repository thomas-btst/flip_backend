import xml.etree.ElementTree as ET
import sys
import os

source_directory = "src"

def getKtFiles():
    kotlin_files = []
    for root, _, files in os.walk(source_directory):
        for file in files:
            if file.endswith(".kt"):
                kotlin_files.append(os.path.join(root, file))
    return kotlin_files


def convert_checkstyle_to_junit(input_file, output_file):
    checkstyle_tree = ET.parse(input_file)
    checkstyle_root = checkstyle_tree.getroot()

    testsuites = ET.Element("testsuites")
    testsuite = ET.SubElement(testsuites, "testsuite", {
        "name": "ktlint",
        "tests": "0",
        "errors": "0",
        "failures": "0",
    })

    files = getKtFiles()

    error_count = 0
    files_with_errors = set()

    for file_element in checkstyle_root.findall("file"):
        file_name = file_element.attrib["name"]
        files_with_errors.add(file_name)
        for error in file_element.findall("error"):
            error_count += 1
            testcase = ET.SubElement(testsuite, "testcase", {
                "name": f"{file_name}:{error.attrib['line']}",
                "classname": "ktlint",
            })
            failure = ET.SubElement(testcase, "failure", {
                "message": error.attrib["message"],
                "type": "checkstyle",
            })
            failure.text = error.attrib["source"]
    
    for file_name in files:
        if file_name not in files_with_errors:
            ET.SubElement(testsuite, "testcase", {
                "name": file_name,
                "classname": "ktlint",
            })

    total_tests = len(files)
    testsuite.attrib["tests"] = str(total_tests)
    testsuite.attrib["failures"] = str(error_count)

    tree = ET.ElementTree(testsuites)
    tree.write(output_file, encoding="utf-8", xml_declaration=True)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python checkstyle_to_junit.py <input_file> <output_file>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]
    convert_checkstyle_to_junit(input_file, output_file)