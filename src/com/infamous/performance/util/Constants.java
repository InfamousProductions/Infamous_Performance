/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * James Roberts
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.infamous.performance.util;

public interface Constants {

    public static final String TAG = "InfamousPerformance";
    public static final String VERSION_NUM = "4.0";
    //hide flashing kernel/recovery options
    // NO_FLASH=true > hide flash options
    // NO_FLASH=false > show flash options
    public static final Boolean NO_FLASH = false;
    public static final Boolean NO_UPDATE = false;
    //hide builtin update
    // NO_UPDATE=true > hide update
    // NO_UPDATE=false > show update
    public static final String URL = "https://play.google.com/store/apps/details?id=com.infamous.performance";
    public static final String PLAYSTORE="https://play.google.com/store/apps/developer?id=Infamous+Productions";
    // CPU settings
    public static final String CPU_ON_PATH = "/sys/devices/system/cpu/cpu0/online";
    public static final String CUR_CPU_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String TEGRA_MAX_FREQ_PATH = "/sys/module/cpu_tegra/parameters/cpu_user_cap";
    public static final String MIN_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    //public static final String STEPS_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String GOVERNORS_LIST_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOVERNOR_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String IO_SCHEDULER_PATH = "/sys/block/mmcblk0/queue/scheduler";
    public static final String IO_TUNABLE_PATH = "/sys/block/mmcblk0/queue/iosched";
    public static final String IO_SOB = "io_settings_sob";

    //Dynamic frequency scaling
    public static final String DYN_MAX_FREQ_PATH = "/sys/power/cpufreq_max_limit";
    public static final String DYN_MIN_FREQ_PATH = "/sys/power/cpufreq_min_limit";

    public static final String HARD_LIMIT_PATH="/sys/kernel/cpufreq/hardlimit";
    
    public static final String NUM_OF_CPUS_PATH = "/sys/devices/system/cpu/present";

    public static final String PREF_MAX_CPU = "pref_max_cpu";
    public static final String PREF_MIN_CPU = "pref_min_cpu";
    public static final String PREF_GOV = "pref_gov";
    public static final String PREF_IO = "pref_io";
    public static final String CPU_SOB = "cpu_sob";
    public static final String GOV_SOB = "gov_settings_sob";
    public static final String GOV_SETTINGS_PATH = "/sys/devices/system/cpu/cpufreq/";

    public static final String MC_PS="/sys/devices/system/cpu/sched_mc_power_savings";//multi core power saving
    public static final String MSM_HOTPLUG="/sys/module/msm_hotplug/msm_enabled";
    public static final String INTELLI_PLUG="/sys/module/intelli_plug/parameters/intelli_plug_active";
    public static final String ECO_MODE="/sys/module/intelli_plug/parameters/eco_mode_active";
    public static final String GEN_HP="/sys/module/omap2plus_cpufreq/parameters/generic_hotplug";//generic hotplug
    public static final String SO_MAX_FREQ="/sys/devices/system/cpu/cpu0/cpufreq/screen_off_max_freq";
    public static final String SO_MIN_FREQ="/sys/devices/system/cpu/cpu0/cpufreq/screen_on_min_freq";

    // CPU info
    public static String KERNEL_INFO_PATH = "/proc/version";
    public static String CPU_INFO_PATH = "/proc/cpuinfo";
    public static String MEM_INFO_PATH = "/proc/meminfo";

    // Time in state
    public static final String TIME_IN_STATE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
    public static final String PREF_OFFSETS = "pref_offsets";
    // Battery
    public static final String BAT_VOLT_PATH = "/sys/class/power_supply/battery/voltage_now";

    // Other settings
    public static final String MINFREE_DEFAULT = "oom_default";
    public static final String MINFREE_PATH = "/sys/module/lowmemorykiller/parameters/minfree";
    public static final String MINFREE_ADJ_PATH = "/sys/module/lowmemorykiller/parameters/adj";
    public static final String READ_AHEAD_PATH ="/sys/block/mmcblk0/queue/read_ahead_kb";
    //"/sys/devices/virtual/bdi/default/read_ahead_kb"

    public static final String PREF_MINFREE = "pref_minfree";
    public static final String PREF_MINFREE_BOOT = "pref_minfree_boot";
    public static final String PREF_READ_AHEAD = "pref_read_ahead";
    public static final String PREF_READ_AHEAD_BOOT = "pref_read_ahead_boot";
    public static final String PREF_FASTCHARGE = "pref_fast_charge";
   //------ MinFree ------
    public static final String OOM_FOREGROUND_APP = "oom_foreground_app";
    public static final String OOM_VISIBLE_APP = "oom_visible_app";
    public static final String OOM_SECONDARY_SERVER = "oom_secondary_server";
    public static final String OOM_HIDDEN_APP = "oom_hidden_app";
    public static final String OOM_CONTENT_PROVIDERS = "oom_content_providers";
    public static final String OOM_EMPTY_APP = "oom_empty_app";
    //------ KSM
    public static final String KSM_RUN_PATH = "/sys/kernel/mm/ksm";
    public static final String UKSM_RUN_PATH = "/sys/kernel/mm/uksm";
    public static final String[] KSM_FULLSCANS_PATH = {"/sys/kernel/mm/ksm/full_scans","/sys/kernel/mm/uksm/full_scans"};
    public static final String[] KSM_PAGESSHARED_PATH = {"/sys/kernel/mm/ksm/pages_shared","/sys/kernel/mm/uksm/pages_shared"};
    public static final String[] KSM_PAGESSHARING_PATH = {"/sys/kernel/mm/ksm/pages_sharing","/sys/kernel/mm/uksm/pages_sharing"};
    public static final String[] KSM_PAGESUNSHERED_PATH = {"/sys/kernel/mm/ksm/pages_unshared","/sys/kernel/mm/uksm/pages_unshared"};
    public static final String[] KSM_PAGESVOLATILE_PATH = {"/sys/kernel/mm/ksm/pages_volatile","/sys/kernel/mm/uksm/pages_volatile"};
    public static final String[] KSM_SLEEP_TIMES_PATH = {"/sys/kernel/mm/ksm/sleep_times","/sys/kernel/mm/uksm/sleep_times"};
    public static final String[] KSM_PAGESSCANNED_PATH = {"/sys/kernel/mm/ksm/pages_scanned","/sys/kernel/mm/uksm/pages_scanned"};
    public static final String PREF_RUN_KSM = "pref_run_ksm";
    public static final String KSM_SOB = "ksm_boot";

    //------ DoNotKillProc
    public static final String USER_PROC_PATH = "/sys/module/lowmemorykiller/parameters/donotkill_proc";
    public static final String SYS_PROC_PATH = "/sys/module/lowmemorykiller/parameters/donotkill_sysproc";
    public static final String USER_PROC_NAMES_PATH = "/sys/module/lowmemorykiller/parameters/donotkill_proc_names";
    public static final String USER_SYS_NAMES_PATH = "/sys/module/lowmemorykiller/parameters/donotkill_sysproc_names";
    public static final String USER_PROC_SOB = "user_proc_boot";
    public static final String SYS_PROC_SOB = "sys_proc_boot";
    public static final String PREF_USER_PROC = "pref_user_proc";
    public static final String PREF_SYS_PROC = "pref_sys_proc";
    public static final String PREF_USER_NAMES = "pref_user_names_proc";
    public static final String PREF_SYS_NAMES = "pref_sys_names_proc";
    //-------BLX---------
    public static final String PREF_BLX = "pref_blx";
    public static final String BLX_PATH = "/sys/class/misc/batterylifeextender/charging_limit";
    public static final String BLX_SOB = "blx_sob";
    //-------DFsync---------
    public static final String DSYNC_PATH = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
    public static final String PREF_DSYNC= "pref_dsync";
    //-------BL----
    public static final String PREF_BLTIMEOUT= "pref_bltimeout";
    public static final String BLTIMEOUT_SOB= "bltimeout_sob";
    public static final String PREF_BLTOUCH= "pref_bltouch";
    public static final String BL_TIMEOUT_PATH="/sys/class/misc/notification/bl_timeout";
    public static final String BL_TOUCH_ON_PATH="/sys/class/misc/notification/touchlight_enabled";
    //-------BLN---------
    public static final String PREF_BLN= "pref_bln";
    //-------PFK---------
    public static final String PFK_VER = "/sys/class/misc/phantom_kp_filter/version";
    public static final String PFK_HOME_ON = "pfk_home_on";
    public static final String PREF_HOME_ALLOWED_IRQ= "pref_home_allowed_irq";
    public static final String PREF_HOME_REPORT_WAIT = "pref_home_report_wait";
    public static final String PFK_MENUBACK_ON = "pfk_menuback_on";
    public static final String PREF_MENUBACK_INTERRUPT_CHECKS = "pref_menuback_interrupt_checks";
    public static final String PREF_MENUBACK_FIRST_ERR_WAIT = "pref_menuback_first_err_wait";
    public static final String PREF_MENUBACK_LAST_ERR_WAIT = "pref_menuback_last_err_wait";

    public static final String PFK_HOME_ENABLED = "/sys/class/misc/phantom_kp_filter/home_enabled";
    public static final String PFK_HOME_ALLOWED_IRQ = "/sys/class/misc/phantom_kp_filter/home_allowed_irqs";
    public static final String PFK_HOME_REPORT_WAIT = "/sys/class/misc/phantom_kp_filter/home_report_wait";
    public static final String PFK_HOME_IGNORED_KP = "/sys/class/misc/phantom_kp_filter/home_ignored_kp";
    public static final String PFK_MENUBACK_ENABLED = "/sys/class/misc/phantom_kp_filter/menuback_enabled";
    public static final String PFK_MENUBACK_INTERRUPT_CHECKS = "/sys/class/misc/phantom_kp_filter/menuback_interrupt_checks";
    public static final String PFK_MENUBACK_FIRST_ERR_WAIT = "/sys/class/misc/phantom_kp_filter/menuback_first_err_wait";
    public static final String PFK_MENUBACK_LAST_ERR_WAIT = "/sys/class/misc/phantom_kp_filter/menuback_last_err_wait";
    public static final String PFK_MENUBACK_IGNORED_KP = "/sys/class/misc/phantom_kp_filter/menuback_ignored_kp";
    public static final String PFK_SOB = "pfk_sob";
    //------------------
    public static final String DYNAMIC_DIRTY_WRITEBACK_PATH = "/proc/sys/vm/dynamic_dirty_writeback";
    public static final String DIRTY_WRITEBACK_ACTIVE_PATH = "/proc/sys/vm/dirty_writeback_active_centisecs";
    public static final String DIRTY_WRITEBACK_SUSPEND_PATH = "/proc/sys/vm/dirty_writeback_suspend_centisecs";
    public static final String PREF_DYNAMIC_DIRTY_WRITEBACK = "pref_dynamic_dirty_writeback";
    public static final String PREF_DIRTY_WRITEBACK_ACTIVE = "pref_dynamic_writeback_active";
    public static final String PREF_DIRTY_WRITEBACK_SUSPEND = "pref_dynamic_writeback_suspend";
    public static final String DYNAMIC_DIRTY_WRITEBACK_SOB = "dynamic_write_back_sob";

    // VM settings
    public static final String VM_SOB = "vm_sob";
    public static final String PREF_VM = "pref_vm";
    public static final String VM_PATH = "/proc/sys/vm/";

    // Voltage control
    public static final String VOLTAGE_SOB = "voltage_sob";
    public static final String UV_MV_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
    public static final String VDD_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/vdd_levels";
    public static final String COMMON_VDD_PATH = "/sys/devices/system/cpu/cpufreq/vdd_levels";
    public static final String VDD_TABLE = "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";
    public static final String VDD_SYSFS_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/vdd_sysfs_levels";

    //Tools
    public static final String PREF_SH = "pref_sh";
    public static final String PREF_WIPE_CACHE = "pref_wipe_cache";
    //public static final String NOT_FOUND = "not found";
    public static final String FLASH_KERNEL = "pref_kernel_img";
    public static final String FLASH_RECOVERY = "pref_recovery_img";
    public static final String RESIDUAL_FILES="pref_residual_files";
    public static final String residualfiles[]={"/data/log","/data/tombstones","/data/system/dropbox","/data/system/usagestats","/data/anr","/data/local/tmp"};//add coresponding info in strings
    public static final String PREF_FIX_PERMS = "pref_fix_perms";
    public static final String PREF_LOG = "pref_log";
    public static final String PREF_OPTIM_DB = "pref_optim_db";

    //Freezer
    public static final String PREF_FRREZE = "freeze_packs";
    public static final String PREF_UNFRREZE = "unfreeze_packs";

    //zRam
    public static final String ISZRAM = "busybox echo `busybox zcat /proc/config.gz | busybox grep ZRAM | busybox grep -v '^#'`";
    public static final String ZRAM_SIZE_PATH = "/sys/block/zram0/disksize";
    public static final String ZRAM_COMPR_PATH = "/sys/block/zram0/compr_data_size";
    public static final String ZRAM_ORIG_PATH = "/sys/block/zram0/orig_data_size";
    public static final String ZRAM_MEMTOT_PATH = "/sys/block/zram0/mem_used_total";
    public static final String PREF_ZRAM = "zram_size";
    public static final String ZRAM_SOB = "zram_boot";
    public static final String ZRAM_ON = "zram_on";

    //sysctl
    public static final String SYSCTL_SOB = "sysctl_sob";

    //touch screen
    public static final String TOUCHSCREEN_SOB = "touchscreen_boot";
    public static final String PREF_SLIDE2WAKE = "pref_slide2wake";
    public static final String PREF_SWIPE2WAKE = "pref_swipe2wake";
    public static final String PREF_HOME2WAKE = "pref_home2wake";
    public static final String PREF_LOGO2WAKE = "pref_logo2wake";
    public static final String PREF_LOGO2MENU = "pref_logo2menu";
    public static final String PREF_POCKET_DETECT = "pref_pocket_detect";
    public static final String PREF_PICK2WAKE = "pref_pick2wake";
    public static final String PREF_DOUBLETAP2WAKE = "pref_doubletap2wake";
    public static final String PREF_FLICK2SLEEP = "pref_flick2sleep";
    public static final String PREF_TOUCH2WAKE = "pref_touch2wake";
    public static final String PREF_FLICK2SLEEP_SENSITIVE="flick2sleep_sensitivity";

    public static final String SLIDE2WAKE="/sys/devices/virtual/sec/tsp/tsp_slide2wake";
    public static final String SWIPE2WAKE="/sys/android_touch/sweep2wake";//0,1,2=off,s2w+s2s,s2s
    public static final String HOME2WAKE="/sys/android_touch/home2wake";
    public static final String LOGO2WAKE="/sys/android_touch/logo2wake";
    public static final String LOGO2MENU="/sys/android_touch/logo2menu";
    public static final String POCKET_DETECT="/sys/android_touch/pocket_detect";
    public static final String PICK2WAKE="/sys/devices/virtual/htc_g_sensor/g_sensor/pick2wake";
    public static final String FLICK2SLEEP="/sys/devices/virtual/htc_g_sensor/g_sensor/flick2sleep";
    public static final String DOUBLETAP2WAKE="/sys/android_touch/doubletap2wake";
    public static final String FLICK2SLEEP_SENSITIVE="/sys/devices/virtual/htc_g_sensor/g_sensor/f2w_sensitivity_values";

    public static final String HOTPLUG_SOB = "hotplug_sob";
    public static final String CPU_QUIET_GOV = "/sys/devices/system/cpu/cpuquiet/available_governors";
    public static final String CPU_QUIET_CUR = "/sys/devices/system/cpu/cpuquiet/current_governor";
    public static final String GPU_MAX_FREQ = "/sys/devices/system/gpu/max_freq";
    public static final String GPU_PARAM_SOB = "gpu_param_sob";

    // PC Settings
    public static final String PREF_USE_LIGHT_THEME = "use_light_theme";
    public static final String PREF_WIDGET_BG_COLOR = "widget_bg_color";
    public static final String PREF_WIDGET_TEXT_COLOR = "widget_text_color";

    //Krait undervolt
    public static final String KRAIT_ON_PATH = "/sys/module/acpuclock_krait/parameters/boost";
    public static final String KRAIT_LOWER_PATH = "/sys/module/acpuclock_krait/parameters/lower_uV";
    public static final String KRAIT_HIGH_PATH = "/sys/module/acpuclock_krait/parameters/higher_uV";
    public static final String KRAIT_THRES_PATH = "/sys/module/acpuclock_krait/parameters/higher_khz_thres";

    //live oc
    public static final String OC_VALUE_PATH = "/sys/class/misc/liveoc/oc_value";
    public static final String OC_HIGH_PATH = "/sys/class/misc/liveoc/oc_target_high";
    public static final String OC_LOW_PATH = "/sys/class/misc/liveoc/oc_target_low";

    public static final String INTENT_PP="com.h0rn3t.performanceprofile.change_system";
    public static final String INIT_D="system/etc/init.d/";
}


