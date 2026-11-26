import com.mountaya.build.targets.AndroidTarget

// An Android module of this SDK, android's target alone, over what every module of it holds
// (`mountaya.kotlin-module`). Its tests run on the host JVM against the android target wherever they
// load nothing native, and on an emulator where they do.
//
// The settings include such a module only where the android target is built, so a run that leaves
// android out never configures one.

plugins {
    id("mountaya.kotlin-module")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    AndroidTarget.configure(project, this, hostTests = true)
}
