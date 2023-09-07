<!--- © Tietoevry 2023 -->
# Purpose

This repository provides a layer for the Yocto project to conveniently build pure and stable matter and otbr binaries for IoT development.


The following binaries and services will be built & installed:
 - `chip-tool`
 - `chip-ota-provider-app`
 - `chip-ota-requestor-app`
 - `ot-ctl`
 - `otbr-agent` & `otbr-agent.service`
 - `otbr-web` & `otbr-web.service`


# Used versions

| Project | Version     |
| ------- | ----------- |
| Matter  | v1.1.0.1    |
| Zap     | v2023.05.04 |
| Thread  | v1.3.0      |


# Tested on

| Board | Version | OS                                                             | Using an external dongle?        |
| ----- | ------- | -------------------------------------------------------------- | -------------------------------- |
| RPi4B | 2GB     | 64-bit Poky (Yocto Project Reference Distro) 4.0.5 (kirkstone) | Yes - [nRF52840 Dongle (PCA10059)](https://www.nordicsemi.com/Products/Development-hardware/nrf52840-dongle) |


# Usage

Add the `matter` keyword to the `LAYERDEPENDS` variable:

```
LAYERDEPENDS_your-layer-name = "your-other-dependencies matter"
```


# Prerequisities

## Python & Node

There are python build constraints that need to be satisfied in order to build matter examples. The safest approach is to create a new python virtual environment and to source it before the `oe-init-build-env` script as:

```
python -m venv mattervenv
source mattervenv/bin/activate
wget https://raw.githubusercontent.com/project-chip/connectedhomeip/v1.0.0.2/scripts/constraints.txt -O matter_python3_constraints.txt
pip install -r matter_python3_constraints.txt
```

This approach also allows us to locally install npm packages which are required by the otbr web frontend. The node and npm installation is not required for this step.

```
pip install nodeenv
nodeenv otbrvenv
source otbrvenv/bin/activate
cd otbrvenv
wget https://raw.githubusercontent.com/openthread/ot-br-posix/7892043367b40c1d1e2402889b716bae6fd17ee7/src/web/web-service/frontend/package.json
npm install
```

Now, you can souce the `oe-init-build-env` script. You should see `(otbrvenv) (mattervenv)` prepended to your command line.


Note: Do not forget to source everything once again if you find yourself in a fresh shell:

```
source ~/mattervenv/bin/activate
source ~/otbrvenv/bin/activate
source ~/Projects/yocto/poky/oe-init-build-env ~/Builds/matter-image
```


## Distribution and Layers

It is recommended to use the [Poky](https://www.yoctoproject.org/software-item/poky/) reference distribution.

Beside that, there are in total three other layer dependencies:
 - [meta-openembedded](https://github.com/openembedded/meta-openembedded.git)
 - [meta-clang](https://github.com/kraj/meta-clang)
 - [meta-browser](https://github.com/OSSystems/meta-browser)

You can clone them by:

```
git clone -b kirkstone https://github.com/openembedded/meta-openembedded.git
git clone -b kirkstone https://github.com/kraj/meta-clang.git
git clone https://github.com/OSSystems/meta-browser.git
```

And add them as:

```
bitbake-layers add-layer ~/Projects/yocto/meta-openembedded/meta-networking/
bitbake-layers add-layer ~/Projects/yocto/meta-clang/
bitbake-layers add-layer ~/Projects/yocto/meta-browser/meta-chromium/
```


# Bluetooth and WiFi support
If you plan to use `ble-wifi`, you need to add `wpa-supplicant` and `bluez5` to your image:

```
IMAGE_INSTALL:append = " your-other-appends wpa-supplicant bluez5"
```


# Services

It is recommended to use `systemd` as the service manager.


# Changes made to the upstream

There are two patches this layer applies to the [connectedhomeip](https://github.com/project-chip/connectedhomeip) (matter) repository:

 - `0001-Add-build_without_pw-to-bypass-the-pw.patch` to build successfuly without pigweed (thanks to the NXP)
 - `0002-Persistent-linux-storage.patch` to move matter settings out of the `/tmp` directory and thus make them persistent (the new directory is `usr/lib/matter`)

And three patches for the [ot-br-posix](https://github.com/openthread/ot-br-posix) repository:

 - `0001-Add-mdns-systemd-option-for-agent-service.patch` to run the `otbr-agent.service` sucessfully via systemd
 - `0002-Add-execstartpre-sleep-for-web-service.patch` to start the `otbr-web.service` with a delay
 - `0004-Add-service-otbr_fwcfg-to-config-the-firewall-policy.patch` to create a firewall configuration service (thanks to the NXP)


# Credit

© Tietoevry 2023

This project aims to provide pure and stable matter and otbr binaries for any board equipped with an ARM processor. The work has been heavily inspired by the work done for [NXP's meta-matter](https://github.com/nxp-imx/meta-matter), so a large part of the credit goes to its creators. That repository is primarily focused on providing matter for NXP boards together with vendor-specific security features


# Contributing

If you want to contribute, feel free to fork and create a new pull request.
