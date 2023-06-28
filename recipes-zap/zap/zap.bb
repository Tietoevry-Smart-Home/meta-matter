# © Tietoevry 2023

PN = "zap-native"
SUMMARY = "ZAP prebuilt tools"
DESCRIPTION = "This recipe provides ZAP prebuilt binaries for the host system"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PACKAGES = "${PN}"

SRC_URI = "https://github.com/project-chip/zap/releases/download/v2023.02.27/zap-linux.zip;unpack=yes"
SRC_URI[sha256sum] = "351da621c5ac5a49b8076a0f58e81273bbae0d3a8d71c611667d8a143e37aa64"

S = "${WORKDIR}"

#INSANE_SKIP:${PN} = " already-stripped arch file-rdeps "
BBCLASSEXTEND = "native"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_FILE_RDEPS = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT_CHECK = "1"
INHIBIT_PACKAGE_DEPMODE_CHECK = "1"
INHIBIT_PACKAGE_RELOCATE = "1"
INHIBIT_PACKAGE_UNPACK = "1"

INSANE_SKIP:${PN} += "dev-so"
inherit native

do_install() {
    install -d -m 0755 ${D}${bindir}/
    cp -ar zap* ${D}${bindir}/
    # This is a workaround to bypass the issue that zap-cli modified by build system
    chmod 444 ${D}${bindir}/zap-cli
}

do_package_qa[noexec] = "1"
EXCLUDE_FROM_SHLIBS = "1"

# This is a workaround to bypass the issue that zap-cli modified by build system
do_deploy() {
    chmod 755 ${D}${bindir}/zap-cli
}

do_populate_sdk:append() {
    chmod 755 ${D}${bindir}/zap-cli
}

addtask deploy after do_install do_populate_sysroot
addtask deploy before do_cleansstate
addtask deploy before do_clean
