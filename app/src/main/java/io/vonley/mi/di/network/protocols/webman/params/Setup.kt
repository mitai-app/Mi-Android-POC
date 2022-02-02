package io.vonley.mi.di.network.protocols.webman.params

import kotlin.reflect.KClass

/**
 * Need Parse Settings App with jsoup,
 * Then decode these params into readable human text
 * this will be fun...
 */

enum class Setup(
    val title: String,
    val param: String,
    value: KClass<*>,
    vararg coupled: CoupledSetup
) {
    //region Scan these devices
    USB0("USB000", "u0", Boolean::class),
    USB1("USB001", "u1", Boolean::class),
    USB2("USB002", "u2", Boolean::class),
    USB3("USB003", "u3", Boolean::class),
    USB6("USB006", "u6", Boolean::class),
    USB7("USB007", "u7", Boolean::class),

    //endregion
    //region Scan for content
    PS3("Playstation 3", "ps3", Boolean::class),
    PS2("Playstation 2", "ps2", Boolean::class),
    P2L("Show PS2 Classic Launcher", "p2l", Boolean::class),
    PS1("Playstation 1", "ps1", Boolean::class),
    PSE("PS1 Net Emu", "pse", Boolean::class),
    PSP("Playstation Portable", "psp", Boolean::class),
    PSL("Show PSP Launcher", "psl", Boolean::class),
    BLU("Blu-ray", "blu", Boolean::class),
    RXV("Show Video Folder", "rxv", Boolean::class),
    DVD("DVD Video", "dvd", Boolean::class),

    //endregion
    LastPlayed("Load last-played game on startup", "lp", Boolean::class), //0,1
    EnableOnStartup(
        "Check for %s on startup",
        "ab",
        Boolean::class,
        CoupledSetup.CheckForOnStartup
    ), // 0,1
    DelayLoadingStartup("Delay loading of %s/last-game (Disc auto-start)", "dy", Boolean::class),
    EnableDevBlind("Enable /dev_blind (writable /dev_flash) on startup", "bl", Boolean::class),
    DisableStartupNotification(
        "Disable startup notification of WebMAN on the XMB",
        "wn",
        String::class
    ), //wmdn?
    DisableContentScanOnStartup("Disable content scan on startup", "rf", String::class), //refr
    DisableUSBPolling("Disable USB polling", "pl", Boolean::class),
    DisableFTP("Disable FTP Service", "ft", Boolean::class),
    DisableAllPadShortcuts("Disable All Pad Shortcuts", "np", Boolean::class),
    DisableRemoteAccessToFTPWWWServices(
        "Disable Remote access to FTP & WWW Services",
        "ip",
        Boolean::class,
        CoupledSetup.DisableRemoteAccessToFTPWWWServices
    ),
    DisableFirmwareSpoofing("Disable firmware spoofing", "nsp", Boolean::class),

    DisableGroupOfContentInWebmanGames(
        "Disable grouping of content in Webman games",
        "ng",
        Boolean::class
    ),
    DisableWebmanSetupEntryInWebmanGames(
        "Disable Webman setup entry in Webman games",
        "ns",
        Boolean::class
    ),
    DisableWebmanCovers("Disable Webman covers", "nc", Boolean::class),
    IncludeTheIdAsPartOfTheTitleOfTheGame("Disable Webman covers", "tid", Boolean::class),
    DisableResetUSBBus("Disable reset USB bus", "bus", Boolean::class),

    EnableDynamicFanControl("Enable Dynamic Fan Control", "fc", Boolean::class),
    DisableTemperatureWarnings("Disable Temperature Warnings", "warn", Boolean::class),
    SetFanMode(
        "Dynamic fan control at: %s C",
        "temp",
        Boolean::class,
        CoupledSetup.SetFanDegree, CoupledSetup.SetFanSpeed
    ), //temp is 0 - Automatic, 1 - Manual
    LowestFanSpeed("Lowest fan speed", "mfam", Int::class), //Lowest Fan Speed
    PS2EmulatorFanSpeed("PS2 emulator fan speed", "fsp0", Int::class),
    ShortcutFailSafe("Fail Safe: SELECT+L3+L2+R2", "pfs", Boolean::class),
    ShortcutShowTemp("Show Temp: SELECT+START", "pss", Boolean::class),
    ShortcutPrevGame("Show Temp: SELECT+START", "ppv", Boolean::class),
    ShortcutNextGame("Show Temp: SELECT+START", "pnx", Boolean::class),
    ShortcutUnloadWM("", "puw", Boolean::class),
    ShortcutCtrlFan("", "pf1", Boolean::class),
    ShortcutCtrlDynFan("Show Temp: SELECT+START", "pdf", Boolean::class),
    ShortcutCtrlMinFan("Show Temp: SELECT+START", "pf2", Boolean::class),
    ShortcutUnmount("Show Temp: SELECT+START", "umt", Boolean::class),
    ShortcutDelCFWSysCalls("Show Temp: SELECT+START", "psc", Boolean::class),
    ShortcutOffline("Show Temp: SELECT+START", "psv", Boolean::class),
    ShortcutCobraToggle("Show Temp: SELECT+START", "pdc", Boolean::class),
    ShortcutGameData("Show Temp: SELECT+START", "pgd", Boolean::class),
    ShortcutNet0("Show Temp: SELECT+START", "pn0", Boolean::class),
    ShortcutNet1("Show Temp: SELECT+START", "pn1", Boolean::class),
    ShortcutPS2Classic("Show Temp: SELECT+START", "p2c", Boolean::class),
    ShortcutPS2Switch("Show Temp: SELECT+START", "p2s", Boolean::class),
    ShortcutRefreshXML("Show Temp: SELECT+START", "pxr", Boolean::class),
    ShortcutShowIDPS("Show Temp: SELECT+START", "pid", Boolean::class),
    ShortcutShutdown("Show Temp: SELECT+START", "psd", Boolean::class),
    ShortcutRestart("Show Temp: SELECT+START", "prs", Boolean::class),

    ID1("IDPS", "id1", Boolean::class, *arrayOf(CoupledSetup.IDPS1, CoupledSetup.IDPS2)),
    ID2("PSID", "id2", Boolean::class, *arrayOf(CoupledSetup.PSID1, CoupledSetup.PSID2)),
    /**
     *
    neth0: 127.0.0.1
    netp0: 38008
    neth1:
    netp1: 38008
    neth2:
    netp2: 38008
    b: 0
    s: 3
    hurl:
    usr: 0
    uacc: 00000001
    fp: 1
    l: 0
     */
}