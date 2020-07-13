#!/usr/bin/python3

import os
import subprocess
import shutil
import re
import sys
from packaging import version


def build_apk():
    gradle_bin = os.path.join(os.getcwd(), "gradlew")
    print(_run_process(gradle_bin, "build"))
    _print_step_finish("Gradle build")


def copy_apk():
    generated_apk_path = os.path.join(os.getcwd(), "app", "build", "outputs", "apk", "debug", "app-debug.apk")
    released_apk_path = _get_last_released_apk_path()
    if not _is_version_incremented(generated_apk_path, released_apk_path):
        sys.exit("Version was not incremented")
    shutil.copy(generated_apk_path, released_apk_path)
    _update_apk_name(released_apk_path)
    _print_step_finish("APK copy")


def _run_process(*args):
    p = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return_code = p.wait()
    if return_code != 0:
        sys.exit(p.stderr.read().decode(errors="replace"))
    return p.stdout.read().decode(errors="replace")


def _print_step_finish(text):
    print("\n{separator}\n{text} finished\n{separator}\n".format(separator=("=" * 50), text=text))


def _get_last_released_apk_path():
    release_dir = os.path.join(os.getcwd(), "release_apk")
    files = os.listdir(release_dir)
    if len(files) != 1:
        sys.exit("Not one file in release directory")
    apk_name = files[0]
    if len(re.findall("^round_calendar_v[\d\.]+\.apk$", apk_name)) != 1:
        sys.exit(f"File has invalid name: {apk_name}")
    return os.path.join(release_dir, apk_name)


def _is_version_incremented(generated_apk_path, released_apk_path):
    gen_version_code, gen_version = _get_version_info(generated_apk_path)
    rel_version_code, rel_version = _get_version_info(released_apk_path)

    if int(gen_version_code) <= int(rel_version_code):
        print(f"Version code check failed\nGenerated apk version code: {gen_version_code}\nLast released apk version "
              f"code: {rel_version_code}")
        return False

    if version.parse(gen_version) <= version.parse(rel_version):
        print(f"Version name check failed\nGenerated apk version name: {gen_version}\nLast released apk version "
              f"name: {rel_version}")
        return False

    return True


def _get_version_info(apk_path):
    result = _run_process("aapt", "dump", "badging", apk_path)
    version_code = re.findall("versionCode='(\d+)'", result)[0]
    version = re.findall("versionName='([\d\.]+)'", result)[0]
    return version_code, version


def _update_apk_name(apk_path):
    _, version = _get_version_info(apk_path)
    curr_version = re.findall("round_calendar_v([\d\.]+)\.apk$", apk_path)[0]
    apk_path_renamed = apk_path.replace(curr_version, version)
    print(f"Released application: {apk_path_renamed}")
    shutil.move(apk_path, apk_path_renamed)


if __name__ == "__main__":
    build_apk()
    copy_apk()
