package com.infamous.performance.util;

import android.util.Log;

import java.io.File;

/**
 * Created by h0rn3t on 13.02.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
public class GPUClass implements Constants{
    private String clkpath=null;
    private String clkvals="";

    public GPUClass(){
        gpu_clk();
        Log.d(TAG, "detect gpu freq path: " + this.clkpath);
    }
    public String gpuclk_path(){
        return this.clkpath;
    }
    public CharSequence[] gpuclk_values(){
        return this.clkvals.split("\\s");
    }
    public CharSequence[] gpuclk_names(){
        CharSequence[] v=gpuclk_values();
        for(int i=0;i<v.length;i++){
            v[i]=Helpers.toMHz(String.valueOf(Integer.parseInt(v[i].toString()) / 1000));
        }
        return v;
    }
    public String gpugovset_path(){
        return gpu_gov_param_path();
    }
    private void gpu_clk() {
        CMDProcessor.CommandResult cr = null;
        if (new File("/sys/class/kgsl/kgsl-3d0/max_gpuclk").exists()) {
            if(new File("/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies").exists()){
                this.clkpath= "/sys/class/kgsl/kgsl-3d0/max_gpuclk";
                this.clkvals=Helpers.readOneLine("/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies");
            }
        }
        else if (new File("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk").exists()) {
            if(new File("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpu_available_frequencies").exists()){
                this.clkpath= "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
                this.clkvals=Helpers.readOneLine("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpu_available_frequencies");
            }
            else{
                cr=new CMDProcessor().sh.runWaitFor("busybox echo `busybox grep 8960 /system/build.prop`");
                if(cr.success()&&cr.stdout.contains("8960")){
                    this.clkpath= "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
                    this.clkvals= "128000000 200000000 300000000 325000000 400000000 480000000 487500000";
                }
                else{
                    cr=new CMDProcessor().sh.runWaitFor("busybox echo `busybox grep 8660 /system/build.prop`");
                    if(cr.success()&&cr.stdout.contains("8660")){
                        this.clkpath= "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
                        this.clkvals= "177000000 200000000 228571000 266667000 300000000 320000000";
                    }
                }
            }
        }
        else if(new File("/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_limit").exists()){
            cr=new CMDProcessor().sh.runWaitFor("busybox echo `busybox find /sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_limit -perm -600`");
            if(cr.success()&&cr.stdout.contains("/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_limit")){
                this.clkpath= "/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_limit";
                this.clkvals=Helpers.readOneLine("/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_list");
            }
        }

    }
    private String gpu_gov_param_path() {
        if (new File("/sys/module/msm_kgsl_core/parameters").exists()) {
            return "/sys/module/msm_kgsl_core/parameters";
        }
        else if (new File("/sys/kernel/debug/tegra_host/scaling").exists()) {
            return "/sys/kernel/debug/tegra_host/scaling";
        }
        else{
            return null;
        }
    }
    private String gpu_gov_path() {
        if (new File("/sys/kernel/pvr_simple_gov/simple_governor").exists()) {
            return "/sys/kernel/pvr_simple_gov/simple_governor";
        }
        else if (new File("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor").exists()) {
            return "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor";
        }
        else if (new File("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor").exists()) {
            return "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor";
        }
        else{
            return null;
        }
    }
}
