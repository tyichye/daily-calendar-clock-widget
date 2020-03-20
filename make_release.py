#!/usr/bin/python3

import os
import subprocess
import shutil
import re


def build_apk():
    gradle_bin = os.path.join(os.getcwd(), "gradlew")
    print(_run_process(gradle_bin, "build"))
    _print_step_finish("Gradle build")


def copy_apk():
    generated_apk_path = os.path.join(os.getcwd(), "app", "build", "outputs", "apk", "debug", "app-debug.apk")
    released_apk_path = _get_last_released_apk_path()
    assert _is_version_incremented(generated_apk_path, released_apk_path)
    shutil.copy(generated_apk_path, released_apk_path)
    _print_step_finish("APK copy")


def _run_process(*args):
    p = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return_code = p.wait()
    assert (return_code == 0), p.stderr.read().decode(errors="replace")
    return p.stdout.read().decode(errors="replace")


def _print_step_finish(text):
    print("\n\n{separator}\n{text} finished\n{separator}".format(separator=("=" * 50), text=text))


def _get_last_released_apk_path():
    release_dir = os.path.join(os.getcwd(), "release_apk")
    files = os.listdir(release_dir)
    assert len(files) == 1
    apk_name = files[0]
    assert len(re.findall("^round_calendar_v\d.\d.apk$", apk_name)) == 1
    return os.path.join(release_dir, apk_name)


def _is_version_incremented(generated_apk_path, released_apk_path):
    gen_version_code, gen_major_version, gen_minor_version = _get_version_info(generated_apk_path)
    rel_version_code, rel_major_version, rel_minor_version = _get_version_info(released_apk_path)

    if int(gen_version_code) <= int(rel_version_code):
        print("Version code check failed\nGenerated apk version code: {}\nLast released apk version code: {}".format(
            gen_version_code, rel_version_code))
        return False

    if int(gen_major_version) <= int(rel_major_version):
        if int(gen_minor_version) <= int(rel_minor_version):
            print("Version name check failed\nGenerated apk version name: {}.{}\nLast released apk version name: "
                  "{}.{}".format(gen_major_version, gen_minor_version, rel_major_version, rel_minor_version))
            return False

    return True


def _get_version_info(apk_path):
    result = _run_process("aapt", "dump", "badging", apk_path)
    version_code = re.findall("versionCode='(\d+)'", result)[0]
    major_version, minor_version = re.findall("versionName='(\d+)\.(\d+)'", result)[0]
    return version_code, major_version, minor_version


if __name__ == "__main__":
    build_apk()
    copy_apk()
