PN = "ot-br-posix-latest"
SUMMARY = "Basic OpenThread support for Yocto"
DESCRIPTION = "This recipe provides otbr-agent, and otbr-web binaries"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRCBRANCH = "main"
SRC_URI = " \
           gitsm://github.com/openthread/ot-br-posix.git;protocol=https;branch=${SRCBRANCH} \
           file://0001-Add-mdns-systemd-option-for-agent-service.patch \
           file://0002-Add-execstartpre-sleep-for-web-service.patch \
           file://0004-Add-service-otbr_fwcfg-to-config-the-firewall-policy.patch \
           "
PATCHTOOL = "git"
# 2023/04/29
SRCREV = "7892043367b40c1d1e2402889b716bae6fd17ee7"
PV = "2023.04+git${SRCPV}"

DEPENDS += " jsoncpp avahi boost pkgconfig-native libnetfilter-queue ipset libnftnl nftables mdns dbus"
RDEPENDS:${PN} += " jsoncpp radvd libnetfilter-queue ipset libnftnl nftables bash mdns dbus"

S = "${WORKDIR}/git"

inherit cmake
EXTRA_OECMAKE = "-DBUILD_TESTING=OFF \
                 -DOTBR_MDNS=mDNSResponder_systemd \
                 -DOTBR_BACKBONE_ROUTER=ON \
                 -DOTBR_BORDER_ROUTING=ON \
                 -DOTBR_DBUS=ON \
                 -DOTBR_DNSSD_DISCOVERY_PROXY=ON \
                 -DOTBR_NAT64=ON \
                 -DOTBR_REST=ON \
                 -DOTBR_SRP_ADVERTISING_PROXY=ON \
                 -DOTBR_WEB=ON \
                 -DOT_LOG_LEVEL=DEBG \
                 "

do_install() {
    install -d -m 755 ${D}${bindir}
    install ${S}/tools/reference_device/otbr_fwcfg.sh ${D}${bindir}

    install -d -m 755 ${D}${sbindir}
    install ${B}/third_party/openthread/repo/src/posix/ot-ctl ${D}${sbindir}
    install ${B}/src/agent/otbr-agent ${D}${sbindir}
    install ${B}/src/web/otbr-web ${D}${sbindir}

    install -d ${D}/etc/default
    install -m 644 ${B}/src/agent/otbr-agent.default ${D}/etc/default/otbr-agent

    install -d ${D}/etc/dbus-1/system.d
    install -m 664 ${B}/src/agent/otbr-agent.conf ${D}/etc/dbus-1/system.d

    install -d ${D}${systemd_system_unitdir}
    install -m 644 ${S}/tools/reference_device/otbr_fwcfg.service ${D}${systemd_system_unitdir}
    install -m 644 ${B}/src/agent/otbr-agent.service ${D}${systemd_system_unitdir}
    install -m 644 ${B}/src/web/otbr-web.service ${D}${systemd_system_unitdir}

    install -d -m 755 ${D}/usr/share/otbr-web/frontend
    cp -R --no-dereference --preserve=mode,timestamps,links -v ${S}/src/web/web-service/frontend/res ${D}/usr/share/otbr-web/frontend
    install -m 644 ${S}/src/web/web-service/frontend/index.html ${D}/usr/share/otbr-web/frontend
    install -m 644 ${S}/src/web/web-service/frontend/join.dialog.html ${D}/usr/share/otbr-web/frontend
    install -m 644 ${B}/src/web/web-service/frontend/node_modules/angular-material/angular-material.min.css ${D}/usr/share/otbr-web/frontend/res/css
    install -m 644 ${B}/src/web/web-service/frontend/node_modules/material-design-lite/material.min.css ${D}/usr/share/otbr-web/frontend/res/css
    install -m 644 ${B}/src/web/web-service/frontend/node_modules/angular-animate/angular-animate.min.js ${D}/usr/share/otbr-web/frontend/res/js
    install -m 644 ${B}/src/web/web-service/frontend/node_modules/angular-aria/angular-aria.min.js ${D}/usr/share/otbr-web/frontend/res/js
    install -m 644 ${B}/src/web/web-service/frontend/node_modules/angular-material/angular-material.min.js ${D}/usr/share/otbr-web/frontend/res/js
    install -m 644 ${B}/src/web/web-service/frontend/node_modules/angular-messages/angular-messages.min.js ${D}/usr/share/otbr-web/frontend/res/js
    install -m 644 ${B}/src/web/web-service/frontend/node_modules/angular/angular.min.js ${D}/usr/share/otbr-web/frontend/res/js
    install -m 644 ${B}/src/web/web-service/frontend/node_modules/d3/d3.min.js ${D}/usr/share/otbr-web/frontend/res/js
    install -m 644 ${B}/src/web/web-service/frontend/node_modules/material-design-lite/material.min.js ${D}/usr/share/otbr-web/frontend/res/js
}

FILES:${PN} += " \
                ${systemd_system_unitdir}/otbr_fwcfg.service \
                ${systemd_system_unitdir}/otbr-agent.service \
                ${systemd_system_unitdir}/otbr-web.service \
                /usr/share/otbr-web/frontend \
                "

inherit systemd
SYSTEMD_SERVICE:${PN} = " \
                         otbr_fwcfg.service \
                         otbr-agent.service \
                         otbr-web.service \
                         "
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
