# © Tietoevry 2023

PN = "matter"
SUMMARY = "Basic Matter IoT connectivity for Yocto"
DESCRIPTION = "This recipe provides chip-tool, chip-ota-provider-app, and chip-ota-requestor-app binaries"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRCBRANCH = "master"
SRC_URI = " \
           gitsm://github.com/project-chip/connectedhomeip.git;protocol=https;branch=${SRCBRANCH} \
           file://0001-Add-build_without_pw-to-bypass-the-pw.patch \
           file://0002-Persistent-linux-storage.patch \
           "
PATCHTOOL = "git"
# v1.0.0.2
#SRCREV = "4088a77f557e8571a39338fad51a1d8eb0131d79"
# v1.1.0.1
SRCREV = "8f66f4215bc0708efc8cc73bda80620e67d8955f"

TARGET_CC_ARCH += "${LDFLAGS}"
DEPENDS += " gn-native ninja-native avahi dbus-glib-native pkgconfig-native zap-native"
RDEPENDS:${PN} += " libavahi-client"

def get_target_cpu(d):
    for arg in (d.getVar('TUNE_FEATURES') or '').split():
        if arg == "cortexa7":
            return 'arm'
        if arg == "armv8a":
            return 'arm64'
    return 'arm64'

def get_arm_arch(d):
    for arg in (d.getVar('TUNE_FEATURES') or '').split():
        if arg == "cortexa7":
            return 'armv7ve'
        if arg == "armv8a":
            return 'armv8-a'
    return 'armv8-a'

def get_arm_cpu(d):
    for arg in (d.getVar('TUNE_FEATURES') or '').split():
        if arg == "cortexa7":
            return 'cortex-a7'
        if arg == "armv8a":
            return 'cortex-a53'
    return 'cortex-a53'

TARGET_CPU = "${@get_target_cpu(d)}"
TARGET_ARM_ARCH = "${@get_arm_arch(d)}"
TARGET_ARM_CPU = "${@get_arm_cpu(d)}"

S = "${WORKDIR}/git"

common_configure() {
    PYTHON_VENV_PATH=$(readlink ${HOSTTOOLS_DIR}/python3)
    PKG_CONFIG_SYSROOT_DIR=${PKG_CONFIG_SYSROOT_DIR} \
    PKG_CONFIG_LIBDIR=${PKG_CONFIG_PATH} \
    gn gen out/aarch64 --script-executable="${PYTHON_VENV_PATH}" --args='treat_warnings_as_errors=false target_os="linux" target_cpu="${TARGET_CPU}" arm_arch="${TARGET_ARM_ARCH}" arm_cpu="${TARGET_ARM_CPU}" build_without_pw=true
        import("//build_overrides/build.gni")
        target_cflags=[
                        "-DCHIP_DEVICE_CONFIG_WIFI_STATION_IF_NAME=\"mlan0\"",
                        "-DCHIP_DEVICE_CONFIG_LINUX_DHCPC_CMD=\"udhcpc -b -i %s \"",
                        "-O3"
                       ]
        custom_toolchain="${build_root}/toolchain/custom"
        target_cc="${CC}"
        target_cxx="${CXX}"
        target_ar="${AR}"'
}

do_configure() {
    cd ${S}/examples/chip-tool
    common_configure

    cd ${S}/examples/ota-provider-app/linux
    common_configure

    cd ${S}/examples/ota-requestor-app/linux
    common_configure
}

do_compile() {
    cd ${S}/examples/chip-tool
    ninja -C out/aarch64

    cd ${S}/examples/ota-provider-app/linux
    ninja -C out/aarch64

    cd ${S}/examples/ota-requestor-app/linux
    ninja -C out/aarch64
}

do_install() {
    install -d -m 755 ${D}${bindir}
    install ${S}/examples/chip-tool/out/aarch64/chip-tool ${D}${bindir}
    install ${S}/examples/ota-provider-app/linux/out/aarch64/chip-ota-provider-app ${D}${bindir}
    install ${S}/examples/ota-requestor-app/linux/out/aarch64/chip-ota-requestor-app ${D}${bindir}

    # Persistent storage
    install -d ${D}${libdir}/matter
}

FILES:${PN} += " \
                /usr/lib/matter \
                "

INSANE_SKIP:${PN} = "ldflags"
