mkdir -p app
keytool -genkeypair \
  -alias androiddebugkey \
  -keypass android \
  -keystore app/debug.keystore \
  -storepass android \
  -storetype PKCS12 \
  -dname "CN=Android Debug,O=Android,C=US" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

cat > generate.sh << 'SCRIPTEOF'
#!/bin/bash
set -e
mkdir -p app/src/main/java/com/yxlanyu/refreshrate/fragment
mkdir -p app/src/main/java/com/yxlanyu/refreshrate/adapter
mkdir -p app/src/main/java/com/yxlanyu/refreshrate/model
mkdir -p app/src/main/java/com/yxlanyu/refreshrate/util
mkdir -p app/src/main/java/com/yxlanyu/refreshrate/service
mkdir -p app/src/main/java/com/yxlanyu/refreshrate/adb
mkdir -p app/src/main/res/layout
mkdir -p app/src/main/res/menu
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/values-en
mkdir -p app/src/main/res/values-ja
mkdir -p app/src/main/res/values-zh-rTW
mkdir -p app/src/main/res/anim
mkdir -p app/src/main/res/drawable
mkdir -p app/src/main/res/mipmap-anydpi-v26
mkdir -p app/src/main/res/xml

cat > avatar_aihaozhe.b64 << 'B64EOF'
/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDABsSFBcUERsXFhceHBsgKEIrKCUlKFE6PTBCYFVlZF9VXVtqeJmBanGQc1tdhbWGkJ6jq62rZ4C8ybqmx5moq6T/2wBDARweHigjKE4rK06kbl1upKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKT/wAARCAABAAEDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDVooooA//Z
B64EOF
base64 -d avatar_aihaozhe.b64 > app/src/main/res/drawable/avatar_aihaozhe.jpg
rm -f avatar_aihaozhe.b64
cp app/src/main/res/drawable/avatar_aihaozhe.jpg app/src/main/res/drawable/avatar_shagua.jpg
cp app/src/main/res/drawable/avatar_aihaozhe.jpg app/src/main/res/drawable/avatar_huaiyin.jpg

cat > settings.gradle << 'EOF'
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral(); maven { url 'https://repo.rikka.app/releases' }; maven { url 'https://jitpack.io' } }
}
rootProject.name = "屏幕刷新率"
include ':app'
EOF

cat > build.gradle << 'EOF'
plugins { id 'com.android.application' version '8.1.0' apply false }
EOF

cat > app/build.gradle << 'EOF'
plugins { id 'com.android.application' }
android {
    namespace 'com.yxlanyu.refreshrate'
    compileSdk 34
    defaultConfig {
        applicationId "com.yxlanyu.refreshrate"
        minSdk 24
        targetSdk 34
        versionCode 1650
        versionName "1.6.5"
    }
    signingConfigs {
        debug {
            storeFile file('debug.keystore')
            storePassword 'android'
            keyAlias 'androiddebugkey'
            keyPassword 'android'
        }
    }
    buildTypes {
        debug {
            signingConfig signingConfigs.debug
            minifyEnabled false
        }
        release {
            minifyEnabled false
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'dev.rikka.shizuku:api:13.1.5'
    implementation 'dev.rikka.shizuku:provider:13.1.5'
    implementation 'com.github.MuntashirAkon:libadb-android:1.0.1'
    implementation 'org.bouncycastle:bcprov-jdk15on:1.69'
    implementation 'org.bouncycastle:bcpkix-jdk15on:1.69'
    implementation 'org.conscrypt:conscrypt-android:2.5.2'
}
EOF

cat > gradle.properties << 'EOF'
org.gradle.jvmargs=-Xmx2048m
android.useAndroidX=true
android.enableJetifier=false
android.suppressUnsupportedCompileSdk=34
EOF

cat > app/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>
    <uses-permission android:name="android.permission.WRITE_SECURE_SETTINGS"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
    <uses-permission android:name="moe.shizuku.manager.permission.API_V23"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/Theme.RefreshRate">
        <activity android:name=".AppearanceActivity" android:exported="false"/>
        <activity android:name=".AnimationActivity" android:exported="false"/>
        <activity android:name=".LanguageActivity" android:exported="false"/>
        <activity android:name=".AppListActivity" android:exported="false"/>
        <activity android:name=".AppRefreshConfigActivity" android:exported="false"/>
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        <provider
            android:name="rikka.shizuku.ShizukuProvider"
            android:authorities="com.yxlanyu.refreshrate.shizuku"
            android:enabled="true"
            android:exported="true"
            android:multiprocess="false"
            android:permission="android.permission.INTERACT_ACROSS_USERS_FULL"/>
        <service
            android:name=".service.OverclockService"
            android:exported="false"
            android:foregroundServiceType="specialUse"
            android:stopWithTask="false" />
        <service
            android:name=".service.RemoteKeepAliveService"
            android:exported="false"
            android:foregroundServiceType="specialUse"
            android:stopWithTask="false" />
        <receiver
            android:name=".service.OverclockRestartReceiver"
            android:exported="false"/>
        <receiver
            android:name=".service.CustomRefreshActionReceiver"
            android:exported="false"/>
        <receiver
            android:name=".service.BootReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED"/>
            </intent-filter>
        </receiver>
        <service
            android:name=".service.KeepAliveAccessibilityService"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.yxlanyu.refreshrate.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths"/>
        </provider>
    </application>
</manifest>
EOF

cat > app/src/main/res/xml/file_paths.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="logs" path="logs/"/>
    <external-files-path name="downloads" path="Download/"/>
</paths>
EOF

cat > app/src/main/res/xml/accessibility_service_config.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault|flagRequestEnhancedWebAccessibility"
    android:canPerformGestures="true"
    android:canRetrieveWindowContent="true"
    android:notificationTimeout="100" />
EOF

cat > app/src/main/res/anim/item_animation.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android" android:duration="450">
    <translate android:fromYDelta="20%" android:toYDelta="0" android:interpolator="@android:anim/decelerate_interpolator"/>
    <alpha android:fromAlpha="0.0" android:toAlpha="1.0" android:interpolator="@android:anim/decelerate_interpolator"/>
</set>
EOF

cat > app/src/main/res/anim/slide_in_right.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android" android:duration="300">
    <translate android:fromXDelta="100%p" android:toXDelta="0" android:interpolator="@android:anim/decelerate_interpolator"/>
</set>
EOF

cat > app/src/main/res/anim/slide_out_left.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android" android:duration="300">
    <translate android:fromXDelta="0" android:toXDelta="-100%p" android:interpolator="@android:anim/decelerate_interpolator"/>
</set>
EOF

cat > app/src/main/res/anim/slide_in_left.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android" android:duration="300">
    <translate android:fromXDelta="-100%p" android:toXDelta="0" android:interpolator="@android:anim/decelerate_interpolator"/>
</set>
EOF

cat > app/src/main/res/anim/slide_out_right.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android" android:duration="300">
    <translate android:fromXDelta="0" android:toXDelta="100%p" android:interpolator="@android:anim/decelerate_interpolator"/>
</set>
EOF

cat > app/src/main/res/drawable/ic_back_arrow.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1A1A2E" android:pathData="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_nav_scheme.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1976D2" android:pathData="M4,18h16v-2H4v2zm0-5h16v-2H4v2zm0-7v2h16V6H4z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_chevron_right.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#888888" android:pathData="M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6z"/>
</vector>
EOF


cat > app/src/main/res/drawable/ic_android_head.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#3DDC84" android:pathData="M6,18c0,0.55 0.45,1 1,1h1v3.5c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5L11,19h2v3.5c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5L16,19h1c0.55,0 1,-0.45 1,-1L18,8L6,8v10zM3.5,8C2.67,8 2,8.67 2,9.5v7c0,0.83 0.67,1.5 1.5,1.5S5,17.33 5,16.5v-7C5,8.67 4.33,8 3.5,8zM20.5,8c-0.83,0 -1.5,0.67 -1.5,1.5v7c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5v-7c0,-0.83 -0.67,-1.5 -1.5,-1.5zM15.53,2.16l1.3,-1.3c0.2,-0.2 0.2,-0.51 0,-0.71 -0.2,-0.2 -0.51,-0.2 -0.71,0l-1.48,1.48C13.85,1.23 12.95,1 12,1c-0.96,0 -1.86,0.23 -2.66,0.63L7.85,0.15c-0.2,-0.2 -0.51,-0.2 -0.71,0 -0.2,0.2 -0.2,0.51 0,0.71l1.31,1.31C6.97,3.26 6,5.01 6,7h12c0,-1.99 -0.97,-3.75 -2.47,-4.84zM10,5L9,5L9,4h1v1zM15,5h-1L14,4h1v1z"/>
</vector>
EOF


cat > app/src/main/res/drawable/ic_switch_check.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#6750A4" android:pathData="M9.55,17.3l-4.7,-4.7 1.4,-1.4 3.3,3.3 7.0,-7.0 1.4,1.4z"/>
</vector>
EOF
cat > app/src/main/res/drawable/ic_check_white.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="20dp" android:height="20dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:pathData="M9.55,17.3l-4.7,-4.7 1.4,-1.4 3.3,3.3 7.0,-7.0 1.4,1.4z"/>
</vector>
EOF
cat > app/src/main/res/drawable/wallpaper_gradient_1.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient android:startColor="#FF9A56" android:endColor="#FF6B95" android:angle="135"/>
</shape>
EOF
cat > app/src/main/res/drawable/seekbar_track_bright.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@android:id/background">
        <shape android:shape="rectangle">
            <corners android:radius="24dp"/>
            <solid android:color="#D8E2EC"/>
        </shape>
    </item>
    <item android:id="@android:id/progress">
        <clip>
            <shape android:shape="rectangle">
                <corners android:radius="24dp"/>
                <solid android:color="#1976D2"/>
            </shape>
        </clip>
    </item>
</layer-list>
EOF

cat > app/src/main/res/drawable/switch_track_md3.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_checked="true">
        <shape android:shape="rectangle">
            <solid android:color="#6750A4"/>
            <corners android:radius="16dp"/>
            <size android:width="33dp" android:height="30dp"/>
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="#E6E0E9"/>
            <stroke android:width="2dp" android:color="#79747E"/>
            <corners android:radius="16dp"/>
            <size android:width="33dp" android:height="30dp"/>
        </shape>
    </item>
</selector>
EOF

cat > app/src/main/res/drawable/switch_thumb_md3.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_checked="true">
        <layer-list>
            <item>
                <shape android:shape="rectangle">
                    <solid android:color="#00000000"/>
                    <size android:width="22dp" android:height="30dp"/>
                </shape>
            </item>
            <item android:gravity="center" android:width="20dp" android:height="20dp">
                <shape android:shape="oval">
                    <solid android:color="#FFFFFF"/>
                </shape>
            </item>
            <item android:drawable="@drawable/ic_switch_check"
                android:gravity="center" android:width="14dp" android:height="14dp"/>
        </layer-list>
    </item>
    <item>
        <layer-list>
            <item>
                <shape android:shape="rectangle">
                    <solid android:color="#00000000"/>
                    <size android:width="22dp" android:height="30dp"/>
                </shape>
            </item>
            <item android:gravity="center" android:width="14dp" android:height="14dp">
                <shape android:shape="oval">
                    <solid android:color="#79747E"/>
                </shape>
            </item>
        </layer-list>
    </item>
</selector>
EOF


cat > app/src/main/res/drawable/ic_update.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1976D2" android:pathData="M12,4V1L8,5l4,4V6c3.31,0 6,2.69 6,6 0,1.01 -0.25,1.97 -0.7,2.8l1.46,1.46C19.54,15.03 20,13.57 20,12c0,-4.42 -3.58,-8 -8,-8zM12,18c-3.31,0 -6,-2.69 -6,-6 0,-1.01 0.25,-1.97 0.7,-2.8L5.24,7.74C4.46,8.97 4,10.43 4,12c0,4.42 3.58,8 8,8v3l4,-4 -4,-4v3z"/>
</vector>
EOF

cat > app/src/main/res/drawable/dialog_round_bg.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#FFFFFF"/>
    <corners android:radius="28dp"/>
    <padding android:left="4dp" android:top="4dp" android:right="4dp" android:bottom="4dp"/>
</shape>
EOF

cat > app/src/main/res/drawable/ic_translate.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#5B57C2" android:pathData="M12.87,15.07l-2.54,-2.51 0.03,-0.03c1.74,-1.94 2.98,-4.17 3.71,-6.53L17,6L17,4h-7L10,2L8,2v2L1,4v1.99h11.17C11.5,7.92 10.44,9.75 9,11.35 8.07,10.32 7.3,9.19 6.69,8h-2c0.73,1.63 1.73,3.17 2.98,4.56l-5.09,5.02L4,19l5,-5 3.11,3.11 0.76,-2.04zM18.5,10h-2L12,22h2l1.12,-3h4.75L21,22h2l-4.5,-12zM15.88,17l1.62,-4.33L19.12,17h-3.24z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_send_plane.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1976D2" android:pathData="M2.01,21L23,12 2.01,3 2,10l15,2 -15,2z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_appearance.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1976D2" android:pathData="M12,3c-4.97,0-9,4.03-9,9s4.03,9 9,9c.83,0 1.5,-0.67 1.5,-1.5 0,-0.39-0.15,-0.74-0.39,-1.01-0.23,-0.26-0.38,-0.61-0.38,-0.99 0,-0.83 0.67,-1.5 1.5,-1.5H16c2.76,0 5,-2.24 5,-5 0,-4.42-4.03,-8-9,-8zM6.5,12c-0.83,0-1.5,-0.67-1.5,-1.5S5.67,9 6.5,9 8,9.67 8,10.5 7.33,12 6.5,12zM9.5,8C8.67,8 8,7.33 8,6.5S8.67,5 9.5,5 11,5.67 11,6.5 10.33,8 9.5,8zM14.5,8c-0.83,0-1.5,-0.67-1.5,-1.5S13.67,5 14.5,5 16,5.67 16,6.5 15.33,8 14.5,8zM17.5,12c-0.83,0-1.5,-0.67-1.5,-1.5S16.67,9 17.5,9 19,9.67 19,10.5 18.33,12 17.5,12z"/>
</vector>
EOF
cat > app/src/main/res/drawable/ic_sun.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFB300" android:pathData="M12,7 A5,5 0 1,0 12,17 A5,5 0 1,0 12,7 Z"/>
    <path android:strokeColor="#FFB300" android:strokeWidth="2" android:strokeLineCap="round" android:pathData="M12,2 L12,4.5 M12,19.5 L12,22 M2,12 L4.5,12 M19.5,12 L22,12 M5,5 L6.8,6.8 M17.2,17.2 L19,19 M19,5 L17.2,6.8 M6.8,17.2 L5,19"/>
</vector>
EOF
cat > app/src/main/res/drawable/ic_launcher_background.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"><solid android:color="#1A1A1A"/></shape>
EOF

cat > app/src/main/res/drawable/ic_launcher_foreground.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFFFF" android:pathData="M16,26 Q16,20 22,20 L86,20 Q92,20 92,26 L92,82 Q92,88 86,88 L22,88 Q16,88 16,82 Z"/>
    <path android:fillColor="#1A1A1A" android:pathData="M20,34 L88,34 L88,84 L20,84 Z"/>
    <path android:fillColor="#FF5F57" android:pathData="M26,27 Q26,24 29,24 Q32,24 32,27 Q32,30 29,30 Q26,30 26,27 Z"/>
    <path android:fillColor="#FEBC2E" android:pathData="M37,27 Q37,24 40,24 Q43,24 43,27 Q43,30 40,30 Q37,30 37,27 Z"/>
    <path android:fillColor="#28C840" android:pathData="M48,27 Q48,24 51,24 Q54,24 54,27 Q54,30 51,30 Q48,30 48,27 Z"/>
    <path android:fillColor="#FFFFFF" android:pathData="M43,66 L72,66 L72,70 L43,70 Z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_nav_home.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1976D2" android:pathData="M10,20V14H14V20H19V12H22L12,3L2,12H5V20H10Z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_nav_remote.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:strokeColor="#1976D2" android:strokeWidth="2.2" android:strokeLineCap="round" android:strokeLineJoin="round" android:fillColor="#00000000" android:pathData="M4,7 L9,12 L4,17"/>
    <path android:strokeColor="#1976D2" android:strokeWidth="2.2" android:strokeLineCap="round" android:fillColor="#00000000" android:pathData="M12,17 L19,17"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_nav_settings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1976D2" android:pathData="M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.06,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.13,5.91 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87c-0.12,0.21 -0.08,0.47 0.12,0.61l2.03,1.58C4.84,11.36 4.82,11.67 4.82,12c0,0.33 0.02,0.64 0.06,0.94L2.85,14.52c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_magisk.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00AF9C"
        android:pathData="M12,1 L21,6.5 L21,17.5 L12,23 L3,17.5 L3,6.5 Z"/>
    <path android:fillColor="#FFFFFF"
        android:pathData="M7.5,8 L10.2,14.5 L12,11 L13.8,14.5 L16.5,8 L14.8,8 L12,13 L9.2,8 Z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_shizuku.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#7C4DFF"
        android:pathData="M3,6 Q3,3 6,3 L18,3 Q21,3 21,6 L21,18 Q21,21 18,21 L6,21 Q3,21 3,18 Z"/>
    <path android:fillColor="#FFFFFF"
        android:pathData="M13,3.5 L8,12.5 L11.5,12.5 L11,20.5 L16,11.5 L12.5,11.5 Z"/>
</vector>
EOF



cat > app/src/main/res/drawable/ic_apps_grid.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1976D2"
        android:pathData="M4,4h4v4H4V4zM10,4h4v4h-4V4zM16,4h4v4h-4V4zM4,10h4v4H4v-4zM10,10h4v4h-4v-4zM16,10h4v4h-4v-4zM4,16h4v4H4v-4zM10,16h4v4h-4v-4zM16,16h4v4h-4v-4z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_search.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1A1A2E"
        android:pathData="M9.5,3C5.9,3 3,5.9 3,9.5S5.9,16 9.5,16c1.6,0 3.1,-0.6 4.2,-1.5L19.2,20 20.5,18.7 15.1,13.2C16,12.1 16,10.8 16,9.5C16,5.9 13.1,3 9.5,3zM9.5,5C12,5 14,7 14,9.5S12,14 9.5,14 5,12 5,9.5 7,5 9.5,5z"/>
</vector>
EOF

cat > app/src/main/res/drawable/ic_more_vert.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#1A1A2E"
        android:pathData="M12,8c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM12,16c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z"/>
</vector>
EOF


cat > app/src/main/res/drawable/wallpaper_gradient_2.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient android:startColor="#6A11CB" android:endColor="#2575FC" android:angle="135"/>
</shape>
EOF

cat > app/src/main/res/drawable/wallpaper_gradient_3.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient android:startColor="#11998E" android:endColor="#38EF7D" android:angle="225"/>
</shape>
EOF

cat > app/src/main/res/drawable/wallpaper_white.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#FFFFFF"/>
</shape>
EOF

cat > app/src/main/res/drawable/glass_highlight.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient
    android:startColor="#80FFFFFF"
    android:centerColor="#25FFFFFF"
    android:endColor="#00FFFFFF"
    android:angle="270"/>
</shape>
EOF

cat > app/src/main/res/drawable/nav_dialog_bg.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#F0F4FF"/>
    <corners android:radius="16dp"/>
</shape>
EOF

cat > app/src/main/res/layout/dialog_nav_scheme.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@drawable/nav_dialog_bg"
    android:padding="24dp">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/nav_scheme_title"
        android:textSize="22sp"
        android:textStyle="bold"
        android:textColor="#1A1A2E"
        android:paddingBottom="12dp"/>
    <RadioGroup
        android:id="@+id/rg_nav_scheme"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <RadioButton
            android:id="@+id/rb_nav_float"
            android:layout_width="match_parent"
            android:layout_height="60dp"
            android:text="@string/nav_scheme_float"
            android:textSize="17sp"
            android:textColor="#1A1A2E"
            android:paddingStart="8dp"
            android:gravity="center_vertical"
            android:buttonTint="#1976D2"/>
        <RadioButton
            android:id="@+id/rb_nav_classic"
            android:layout_width="match_parent"
            android:layout_height="60dp"
            android:text="@string/nav_scheme_classic"
            android:textSize="17sp"
            android:textColor="#1A1A2E"
            android:paddingStart="8dp"
            android:gravity="center_vertical"
            android:buttonTint="#1976D2"/>
        <RadioButton android:id="@+id/rb_nav_top" android:layout_width="match_parent" android:layout_height="60dp" android:text="@string/nav_scheme_top" android:textSize="17sp" android:textColor="#1A1A2E" android:paddingStart="8dp" android:gravity="center_vertical" android:buttonTint="#1976D2"/>
        <RadioButton android:id="@+id/rb_nav_left" android:layout_width="match_parent" android:layout_height="60dp" android:text="@string/nav_scheme_left" android:textSize="17sp" android:textColor="#1A1A2E" android:paddingStart="8dp" android:gravity="center_vertical" android:buttonTint="#1976D2"/>
        <RadioButton android:id="@+id/rb_nav_right" android:layout_width="match_parent" android:layout_height="60dp" android:text="@string/nav_scheme_right" android:textSize="17sp" android:textColor="#1A1A2E" android:paddingStart="8dp" android:gravity="center_vertical" android:buttonTint="#1976D2"/>
    </RadioGroup>
</LinearLayout>
EOF

cat > app/src/main/res/layout/dialog_single_choice.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android" android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:background="@drawable/nav_dialog_bg" android:padding="24dp">
    <TextView android:id="@+id/dialog_choice_title" android:layout_width="wrap_content" android:layout_height="wrap_content" android:textSize="22sp" android:textStyle="bold" android:textColor="#1A1A2E" android:paddingBottom="12dp"/>
    <RadioGroup android:id="@+id/dialog_choice_group" android:layout_width="match_parent" android:layout_height="wrap_content"/>
</LinearLayout>
EOF

cat > app/src/main/res/layout/item_app_entry.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="76dp"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingStart="18dp"
    android:paddingEnd="16dp"
    android:background="?attr/selectableItemBackground">
    <ImageView
        android:id="@+id/iv_app_icon"
        android:layout_width="46dp"
        android:layout_height="46dp"
        android:contentDescription="@string/app_name"/>
    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical"
        android:layout_marginStart="18dp">
        <TextView
            android:id="@+id/tv_app_name"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="17sp"
            android:textColor="@color/text_dark"
            android:maxLines="1"
            android:ellipsize="end"/>
        <TextView
            android:id="@+id/tv_app_package"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="3dp"
            android:textSize="14sp"
            android:textColor="@color/text_muted"
            android:maxLines="1"
            android:ellipsize="end"/>
    </LinearLayout>
</LinearLayout>
EOF

cat > app/src/main/res/layout/activity_app_list.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:fitsSystemWindows="true"
    android:background="@color/bg_main">
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="82dp"
        android:paddingStart="16dp"
        android:paddingEnd="12dp"
        android:background="#55FFFFFF">
        <ImageView
            android:id="@+id/btn_back"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_alignParentStart="true"
            android:layout_centerVertical="true"
            android:padding="8dp"
            android:src="@drawable/ic_back_arrow"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="@string/permission_cancel"/>
        <TextView
            android:id="@+id/tv_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_toEndOf="@id/btn_back"
            android:layout_centerVertical="true"
            android:layout_marginStart="16dp"
            android:text="@string/app_management_title"
            android:textSize="26sp"
            android:textColor="@color/text_dark"
            android:textStyle="bold"/>
        <ImageView
            android:id="@+id/btn_more"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_alignParentEnd="true"
            android:layout_centerVertical="true"
            android:padding="8dp"
            android:src="@drawable/ic_more_vert"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="@string/show_system_apps"/>
        <ImageView
            android:id="@+id/btn_search"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_toStartOf="@id/btn_more"
            android:layout_centerVertical="true"
            android:padding="8dp"
            android:src="@drawable/ic_search"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="@string/search_app_hint"/>
    </RelativeLayout>
    <EditText
        android:id="@+id/et_search"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"
        android:layout_marginBottom="6dp"
        android:hint="@string/search_app_hint"
        android:singleLine="true"
        android:visibility="gone"
        android:backgroundTint="@color/primary"/>
    <TextView
        android:id="@+id/tv_app_list_hint"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingStart="18dp"
        android:paddingEnd="18dp"
        android:paddingTop="8dp"
        android:paddingBottom="8dp"
        android:text="@string/app_list_third_party_hint"
        android:textSize="12sp"
        android:textColor="@color/text_muted"/>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_apps"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>
    <TextView
        android:id="@+id/tv_empty"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:padding="20dp"
        android:visibility="gone"
        android:text="@string/no_apps_found"
        android:textColor="@color/text_muted"/>
</LinearLayout>
EOF

cat > app/src/main/res/layout/activity_app_refresh_config.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:fitsSystemWindows="true"
    android:background="@color/bg_main">
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="82dp"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:background="#55FFFFFF">
        <ImageView
            android:id="@+id/btn_back"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_alignParentStart="true"
            android:layout_centerVertical="true"
            android:padding="8dp"
            android:src="@drawable/ic_back_arrow"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="@string/permission_cancel"/>
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_toEndOf="@id/btn_back"
            android:layout_centerVertical="true"
            android:layout_marginStart="16dp"
            android:text="@string/app_refresh_config_title"
            android:textSize="26sp"
            android:textColor="@color/text_dark"
            android:textStyle="bold"/>
    </RelativeLayout>
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp"
        android:clipToPadding="false">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingBottom="24dp">
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="14dp"
                app:cardCornerRadius="14dp"
                app:cardElevation="0dp"
                app:cardBackgroundColor="#AAFFFFFF">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:padding="16dp">
                    <ImageView
                        android:id="@+id/iv_app_icon"
                        android:layout_width="54dp"
                        android:layout_height="54dp"/>
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical"
                        android:layout_marginStart="16dp">
                        <TextView
                            android:id="@+id/tv_app_name"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:textSize="20sp"
                            android:textStyle="bold"
                            android:textColor="@color/text_dark"
                            android:maxLines="1"
                            android:ellipsize="end"/>
                        <TextView
                            android:id="@+id/tv_app_package"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:textSize="14sp"
                            android:textColor="@color/text_muted"
                            android:maxLines="2"
                            android:ellipsize="end"/>
                    </LinearLayout>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:cardCornerRadius="14dp"
                app:cardElevation="0dp"
                app:cardBackgroundColor="#AAFFFFFF">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="center_vertical">
                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical">
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="@string/enable_single_app_refresh"
                                android:textSize="16sp"
                                android:textStyle="bold"
                                android:textColor="@color/text_dark"/>
                            <TextView
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="4dp"
                                android:text="@string/single_app_refresh_desc"
                                android:textSize="12sp"
                                android:textColor="@color/text_muted"/>
                        </LinearLayout>
                        <androidx.appcompat.widget.SwitchCompat
                            app:track="@drawable/switch_track_md3"
                            android:switchMinWidth="33dp"
                            android:thumbTextPadding="0dp"
                            app:thumbTint="@null"
                            app:trackTint="@null"
                            android:thumb="@drawable/switch_thumb_md3"
                            android:id="@+id/switch_single_app_refresh"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"/>
                    </LinearLayout>
                    <LinearLayout
                        android:id="@+id/ll_app_target_container"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:layout_marginTop="18dp"
                        android:visibility="gone">
                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical"
                            android:layout_marginEnd="8dp">
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="@string/target_resolution_label"
                                android:textSize="13sp"
                                android:textColor="@color/text_muted"/>
                            <Spinner
                                android:id="@+id/spinner_app_resolution"
                                android:layout_width="match_parent"
                                android:layout_height="48dp"/>
                        </LinearLayout>
                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical"
                            android:layout_marginStart="8dp">
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="@string/target_rate_label"
                                android:textSize="13sp"
                                android:textColor="@color/text_muted"/>
                            <Spinner
                                android:id="@+id/spinner_app_rate"
                                android:layout_width="match_parent"
                                android:layout_height="48dp"/>
                        </LinearLayout>
                    </LinearLayout>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</LinearLayout>
EOF

cat > app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@drawable/ic_launcher_background"/><foreground android:drawable="@drawable/ic_launcher_foreground"/></adaptive-icon>
EOF

cat > app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@drawable/ic_launcher_background"/><foreground android:drawable="@drawable/ic_launcher_foreground"/></adaptive-icon>
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/WallpaperStore.java << 'EOF'
package com.yxlanyu.refreshrate;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
public class WallpaperStore {
    private static final String FILE_NAME = "wallpaper_img";
    private static Bitmap cached = null;
    public static java.io.File file(Context ctx) {
        return new java.io.File(ctx.getFilesDir(), FILE_NAME);
    }
    public static boolean saveFromUri(Context ctx, Uri uri) {
        java.io.InputStream is = null;
        java.io.FileOutputStream fos = null;
        java.io.File tmp = new java.io.File(ctx.getFilesDir(), FILE_NAME + ".tmp");
        try {
            is = ctx.getContentResolver().openInputStream(uri);
            if (is == null) return false;
            fos = new java.io.FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int len;
            long total = 0;
            while ((len = is.read(buf)) != -1) { fos.write(buf, 0, len); total += len; }
            fos.flush();
            if (total <= 0) { tmp.delete(); return false; }
            java.io.File dst = file(ctx);
            if (dst.exists()) dst.delete();
            if (!tmp.renameTo(dst)) { tmp.delete(); return false; }
            cached = null;
            ctx.getSharedPreferences("s", Context.MODE_PRIVATE).edit()
                    .putString("wallpaper_path", dst.getAbsolutePath()).apply();
            return true;
        } catch (Exception e) {
            try { tmp.delete(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (is != null) is.close(); } catch (Exception ignored) {}
            try { if (fos != null) fos.close(); } catch (Exception ignored) {}
        }
    }
    public static Bitmap load(Context ctx) {
        if (cached != null && !cached.isRecycled()) return cached;
        try {
            java.io.File f = file(ctx);
            if (f.exists() && f.length() > 0) {
                Bitmap bm = decodeScaled(f);
                if (bm != null) { cached = bm; return bm; }
            }
            String uriStr = ctx.getSharedPreferences("s", Context.MODE_PRIVATE).getString("wallpaper_uri", null);
            if (uriStr != null) {
                if (saveFromUri(ctx, Uri.parse(uriStr))) {
                    Bitmap bm = decodeScaled(file(ctx));
                    if (bm != null) { cached = bm; return bm; }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
    private static final java.util.HashMap<String, Bitmap> pageCache = new java.util.HashMap<String, Bitmap>();
    public static java.io.File pageFile(Context ctx, String key) {
        return new java.io.File(ctx.getFilesDir(), "wp_page_" + key);
    }
    public static boolean savePageFromUri(Context ctx, Uri uri, String key) {
        java.io.InputStream is = null;
        java.io.FileOutputStream fos = null;
        java.io.File tmp = new java.io.File(ctx.getFilesDir(), "wp_page_" + key + ".tmp");
        try {
            is = ctx.getContentResolver().openInputStream(uri);
            if (is == null) return false;
            fos = new java.io.FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int len;
            long total = 0;
            while ((len = is.read(buf)) != -1) { fos.write(buf, 0, len); total += len; }
            fos.flush();
            if (total <= 0) { tmp.delete(); return false; }
            java.io.File dst = pageFile(ctx, key);
            if (dst.exists()) dst.delete();
            if (!tmp.renameTo(dst)) { tmp.delete(); return false; }
            pageCache.remove(key);
            return true;
        } catch (Exception e) {
            try { tmp.delete(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (is != null) is.close(); } catch (Exception ignored) {}
            try { if (fos != null) fos.close(); } catch (Exception ignored) {}
        }
    }
    public static Bitmap loadPage(Context ctx, String key) {
        Bitmap c = pageCache.get(key);
        if (c != null && !c.isRecycled()) return c;
        try {
            java.io.File f = pageFile(ctx, key);
            if (f.exists() && f.length() > 0) {
                Bitmap bm = decodeScaled(f);
                if (bm != null) { pageCache.put(key, bm); return bm; }
            }
        } catch (Exception ignored) {}
        return null;
    }
    public static boolean hasPage(Context ctx, String key) {
        java.io.File f = pageFile(ctx, key);
        return f.exists() && f.length() > 0;
    }
    public static void clearPage(Context ctx, String key) {
        try { java.io.File f = pageFile(ctx, key); if (f.exists()) f.delete(); } catch (Exception ignored) {}
        pageCache.remove(key);
    }
    public static android.graphics.drawable.Drawable presetDrawable(Context ctx, int type) {
        switch (type) {
            case 1: return androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.wallpaper_gradient_2);
            case 2: return androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.wallpaper_gradient_3);
            case 6: return androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.wallpaper_gradient_1);
            case 5: return new android.graphics.drawable.ColorDrawable(0xFF000000);
            case 7: return new android.graphics.drawable.ColorDrawable(0xFF4FC3F7);
            case 8: return new android.graphics.drawable.ColorDrawable(0xFFF48FB1);
            case 9: return new android.graphics.drawable.ColorDrawable(0xFF80CBC4);
            case 10: return new android.graphics.drawable.ColorDrawable(0xFFB39DDB);
            default: return new android.graphics.drawable.ColorDrawable(0xFFFFFFFF);
        }
    }
    private static Bitmap decodeScaled(java.io.File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), o);
            int maxSide = 2400;
            int sample = 1;
            int w = o.outWidth, h = o.outHeight;
            while (w / sample > maxSide || h / sample > maxSide) sample *= 2;
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sample;
            return BitmapFactory.decodeFile(f.getAbsolutePath(), o2);
        } catch (Exception e) { return null; }
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/util/LanguageUtils.java << 'EOF'
package com.yxlanyu.refreshrate.util;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Locale;
public class LanguageUtils {
    public static final String LANG_ZH = "zh";
    public static final String LANG_ZH_TW = "zh-rTW";
    public static final String LANG_EN = "en";
    public static final String LANG_JA = "ja";
    public static final String LANG_RU = "ru";
    public static final String LANG_KO = "ko";
    public static final String LANG_SYSTEM = "system";
    private static final String PREFS_NAME = "s";
    private static final String KEY_LANG = "language";
    public static void applyLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANG, LANG_ZH);
        setLocale(context, lang);
    }
    public static void setLanguageAndRecreate(Activity activity, String lang) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANG, lang).apply();
        setLocale(activity, lang);
        activity.recreate();
    }
    public static String getCurrentLang(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, LANG_ZH);
    }
    private static Locale resolve(String lang) {
        if (LANG_SYSTEM.equals(lang)) {
            Configuration sys = Resources.getSystem().getConfiguration();
            return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    ? sys.getLocales().get(0) : sys.locale;
        } else if (LANG_ZH_TW.equals(lang)) {
            return new Locale("zh", "TW");
        }
        return new Locale(lang);
    }
    public static Context wrap(Context context) {
        String lang = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANG, LANG_ZH);
        Locale locale = resolve(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.createConfigurationContext(config);
        }
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        return context;
    }
    private static void setLocale(Context context, String lang) {
        Locale locale = resolve(lang);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/model/DisplayMode.java << 'EOF'
package com.yxlanyu.refreshrate.model;
import android.content.Context;
import com.yxlanyu.refreshrate.R;
public class DisplayMode {
    private int width, height, modeId;
    private float refreshRate;
    private int sfIndex = -1;
    public DisplayMode(int w, int h, float r, int id) {
        width=w;
        height=h; refreshRate=r; modeId=id;
    }
    public int getWidth(){return width;}
    public int getHeight(){return height;}
    public float getRefreshRate(){return refreshRate;}
    public int getModeId(){return modeId;}
    public int getSfIndex(){return sfIndex;}
    public void setSfIndex(int i){sfIndex=i;}
    public String getResolutionLabel(){return width+"x"+height;}
    public int getRateInt(){return Math.round(refreshRate);}
    public String getRateName(Context ctx) {
    int r = getRateInt();
    if (r >= 185) return ctx.getString(R.string.rate_name_supreme);
    if (r >= 165) return ctx.getString(R.string.rate_name_limit);
    if (r >= 144) return ctx.getString(R.string.rate_name_high);
    if (r >= 120) return ctx.getString(R.string.rate_name_extreme);
    if (r >= 90)  return ctx.getString(R.string.rate_name_excellent);
    if (r >= 60)  return ctx.getString(R.string.rate_name_smooth);
    return ctx.getString(R.string.rate_name_standard);
}
public String getRateDesc(Context ctx) {
    int r = getRateInt();
    if (r >= 185) return ctx.getString(R.string.rate_desc_supreme);
    if (r >= 165) return ctx.getString(R.string.rate_desc_limit);
    if (r >= 144) return ctx.getString(R.string.rate_desc_high);
    if (r >= 120) return ctx.getString(R.string.rate_desc_extreme);
    if (r >= 90)  return ctx.getString(R.string.rate_desc_excellent);
    if (r >= 60)  return ctx.getString(R.string.rate_desc_smooth);
    return ctx.getString(R.string.rate_desc_standard);
}
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/util/RootUtils.java << 'EOF'
package com.yxlanyu.refreshrate.util;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.yxlanyu.refreshrate.model.DisplayMode;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class RootUtils {
    private static final String TAG = "RootUtils";
    public static boolean isRooted() {
        Process p = null;
        DataOutputStream os = null; BufferedReader r = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo RootOK\nexit\n"); os.flush();
            r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return "RootOK".equals(r.readLine());
        } catch (Exception e) { return false;
        } finally {
            try { if(os != null) os.close();
            } catch(Exception ignored){}
            try { if(r != null) r.close();
            } catch(Exception ignored){}
            try { if(p != null) p.destroy();
            } catch(Exception ignored){}
        }
    }
    public static List<DisplayMode> getDisplayModesFromDumpsys() {
        List<DisplayMode> list = new ArrayList<>();
        Process p = null; DataOutputStream os = null; BufferedReader r = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("dumpsys display | grep 'DisplayModeRecord'\nexit\n");
            os.flush();
            r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            Pattern pattern = Pattern.compile("id=(\\d+),\\s*width=(\\d+),\\s*height=(\\d+),\\s*fps=([\\d.]+)");
            int sfIdx = 0;
            while ((line = r.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    int id = Integer.parseInt(m.group(1));
                    int w = Integer.parseInt(m.group(2));
                    int h = Integer.parseInt(m.group(3));
                    float fps = Float.parseFloat(m.group(4));
                    DisplayMode dmode = new DisplayMode(w, h, fps, id);
                    dmode.setSfIndex(sfIdx++);
                    list.add(dmode);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "解析dumpsys失败", e);
        } finally {
            try { if(os != null) os.close();
            } catch(Exception ignored){}
            try { if(r != null) r.close();
            } catch(Exception ignored){}
            try { if(p != null) p.destroy();
            } catch(Exception ignored){}
        }
        return list;
    }
    public static String buildSwitchScript(int w, int h, int hz, int sfIndex, java.util.List<Integer> ladder) {
        StringBuilder s = new StringBuilder();
        if (w > 0 && h > 0 && hz > 0)
            s.append("cmd display set-user-preferred-display-mode ").append(w).append(" ").append(h).append(" ").append(hz).append(" 2>/dev/null; ");
        s.append("settings put system peak_refresh_rate ").append(hz).append(".0; ");
        s.append("settings put system min_refresh_rate ").append(hz).append(".0; ");
        s.append("settings put system user_refresh_rate ").append(hz).append("; ");
        s.append("settings put secure miui_refresh_rate ").append(hz).append("; ");
        if (sfIndex >= 0)
            s.append("service call SurfaceFlinger 1035 i32 ").append(sfIndex).append(" >/dev/null 2>&1");
        return s.toString();
    }
    private static final java.util.concurrent.atomic.AtomicInteger SWITCH_SEQ = new java.util.concurrent.atomic.AtomicInteger(0);
    public static boolean setDisplayMode(int w, int h, int targetHz, int sfIndex) {
        return doSetDisplayMode(w, h, targetHz, sfIndex, SWITCH_SEQ.incrementAndGet());
    }
    private static synchronized boolean doSetDisplayMode(int w, int h, int targetHz, int sfIndex, int mySeq) {
        if (mySeq != SWITCH_SEQ.get()) return true;
        runSu(buildSwitchScript(w, h, targetHz, sfIndex, null));
        return true;
    }
    private static void runSu(String cmd) {
        Process p = null;
        DataOutputStream os = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(cmd + "\nexit\n"); os.flush();
            p.waitFor();
        } catch (Exception ignored) {
        } finally {
            try { if (os != null) os.close(); } catch (Exception ig) {}
            try { if (p != null) p.destroy(); } catch (Exception ig) {}
        }
    }
    private static int activeModeId() {
        Process p = null;
        DataOutputStream os = null;
        BufferedReader br = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("dumpsys display | grep -m1 mActiveModeId\nexit\n"); os.flush();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            Pattern pat = Pattern.compile("mActiveModeId=(\\d+)");
            while ((line = br.readLine()) != null) {
                Matcher mm = pat.matcher(line);
                if (mm.find()) return Integer.parseInt(mm.group(1));
            }
        } catch (Exception ignored) {
        } finally {
            try { if (br != null) br.close(); } catch (Exception ig) {}
            try { if (os != null) os.close(); } catch (Exception ig) {}
            try { if (p != null) p.destroy(); } catch (Exception ig) {}
        }
        return -1;
    }
    public static boolean restoreAdaptive(int minHz, int maxHz) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("cmd display clear-user-preferred-display-mode 2>/dev/null\n");
            if (minHz > 0) os.writeBytes("settings put system min_refresh_rate " + minHz + ".0\n");
            if (maxHz > 0) os.writeBytes("settings put system peak_refresh_rate " + maxHz + ".0\n");
            os.writeBytes("exit\n"); os.flush();
            process.waitFor();
            return true;
        } catch (Exception e) { return false;
        } finally {
            try { if(os != null) os.close(); } catch(Exception ignored){}
            try { if(process != null) process.destroy(); } catch(Exception ignored){}
        }
    }
    public static boolean setNativeRefreshOverlay(boolean on) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("service call SurfaceFlinger 1034 i32 " + (on ? 1 : 0) + "\n");
            os.writeBytes("exit\n"); os.flush();
            process.waitFor();
            return true;
        } catch (Exception e) { return false;
        } finally {
            try { if(os != null) os.close(); } catch(Exception ignored){}
            try { if(process != null) process.destroy(); } catch(Exception ignored){}
        }
    }
    public static String generateRuntimeLog() { return generateRuntimeLog(null); }
    public static String generateRuntimeLog(Context ctx) {
    StringBuilder sb = new StringBuilder();
    String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(new java.util.Date());
    sb.append("===== 屏幕刷新率工具 运行调试日志 =====\n");
    sb.append("生成时间: ").append(ts).append("\n");
    sb.append("App版本: 1.6.5 (1650)\n\n");
    sb.append("【设备信息】\n");
    sb.append("品牌: ").append(android.os.Build.BRAND).append("\n");
    sb.append("型号: ").append(android.os.Build.MODEL).append("\n");
    sb.append("Android: ").append(android.os.Build.VERSION.RELEASE)
      .append("  (API ").append(android.os.Build.VERSION.SDK_INT).append(")\n");
    sb.append("ROM: ").append(android.os.Build.DISPLAY).append("\n\n");
    sb.append("【授权状态】\n");
    boolean rooted = isRooted();
    sb.append("Root: ").append(rooted ? "✓ 已获取" : "Ⅹ 未获取").append("\n");
    try {
        boolean shAvail = ShizukuUtils.isAvailable();
        boolean shPerm  = shAvail && ShizukuUtils.hasPermission();
        sb.append("Shizuku 服务: ").append(shAvail ? "✓ 运行中" : "Ⅹ 未运行/未安装").append("\n");
        sb.append("Shizuku 授权: ").append(shPerm ? "✓ 已授权" : "Ⅹ 未授权").append("\n");
    } catch (Throwable t) {
        sb.append("Shizuku: 检测异常 ").append(t.getMessage()).append("\n");
    }
    if (ctx != null) {
        SharedPreferences prefs = ctx.getSharedPreferences("s", Context.MODE_PRIVATE);
        String authMode = prefs.getString("auth_mode", "");
        sb.append("当前授权模式: ").append(authMode.isEmpty() ? "未选择" : authMode).append("\n");
        boolean a11y = AccessibilityUtils.isKeepAliveServiceEnabled(ctx);
        sb.append("无障碍服务: ").append(a11y ? "✓ 已开启" : "Ⅹ 未开启").append("\n");
        boolean notifOn = androidx.core.app.NotificationManagerCompat.from(ctx).areNotificationsEnabled();
        sb.append("通知权限: ").append(notifOn ? "✓ 已允许" : "Ⅹ 已被关闭").append("\n");
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            boolean postPerm = ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
            sb.append("POST_NOTIFICATIONS: ").append(postPerm ? "✓ 已授予" : "Ⅹ 未授予").append("\n");
        }
        try {
            android.os.PowerManager pm = (android.os.PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
            boolean ignoreBatt = pm != null && pm.isIgnoringBatteryOptimizations(ctx.getPackageName());
            sb.append("忽略电池优化: ").append(ignoreBatt ? "✓ 是" : "Ⅹ 否(可能被杀后台)").append("\n");
        } catch (Exception ignored) {}
    } else {
        sb.append("（无障碍/通知状态需在设置页生成才可读取）\n");
    }
    sb.append("\n");
    if (ctx != null) {
        SharedPreferences prefs = ctx.getSharedPreferences("s", Context.MODE_PRIVATE);
        sb.append("【功能状态】\n");
        sb.append("自定义应用刷新率: ").append(prefs.getBoolean("custom_app_refresh", false) ? "开" : "关").append("\n");
        sb.append("自动超频: ").append(prefs.getBoolean("auto_overclock", false) ? "开" : "关")
          .append("  (运行中: ").append(com.yxlanyu.refreshrate.util.AutoOverclockManager.isRunning() ? "是" : "否").append(")\n");
        sb.append("超频目标: ").append(prefs.getString("oc_target_res", "未设置"))
          .append(" @ ").append(prefs.getInt("oc_target_hz", -1)).append("Hz\n");
        sb.append("锁定刷新率: ").append(prefs.getBoolean("rate_lock_enabled", false)
                ? ("锁定中 @ " + prefs.getInt("rate_lock_hz", 0) + "Hz") : "未锁定").append("\n");
        sb.append("超频最近日志: ").append(com.yxlanyu.refreshrate.util.AutoOverclockManager.getLastLog()).append("\n\n");
        sb.append("【已配置的单应用刷新率】\n");
        int cfgCount = 0;
        Map<String, ?> all = prefs.getAll();
        for (String k : all.keySet()) {
            if (k.startsWith("app_refresh_enabled_") && Boolean.TRUE.equals(all.get(k))) {
                String pkg = k.substring("app_refresh_enabled_".length());
                String res = prefs.getString("app_refresh_res_" + pkg, "?");
                int hz = prefs.getInt("app_refresh_hz_" + pkg, -1);
                sb.append("  • ").append(pkg).append(" → ").append(res).append(" @ ").append(hz).append("Hz\n");
                cfgCount++;
            }
        }
        if (cfgCount == 0) sb.append("  （暂无）\n");
        sb.append("\n");
        sb.append("【当前显示状态】\n");
        sb.append("当前分辨率: ").append(com.yxlanyu.refreshrate.util.AutoOverclockManager.getCurrentResolution(ctx)).append("\n");
        sb.append("当前刷新率: ").append(Math.round(com.yxlanyu.refreshrate.util.AutoOverclockManager.getCurrentRate(ctx))).append("Hz\n");
        sb.append("系统支持档位数: ").append(com.yxlanyu.refreshrate.util.AutoOverclockManager.getSupportedModes(ctx).size()).append("\n");
        sb.append("【档位ID映射(系统id↔软件执行id)】\n");
        for (com.yxlanyu.refreshrate.model.DisplayMode dm : com.yxlanyu.refreshrate.util.AutoOverclockManager.getSupportedModes(ctx)) {
            sb.append("  ").append(dm.getWidth()).append("×").append(dm.getHeight())
              .append(" @ ").append(dm.getRateInt()).append("Hz  系统id=").append(dm.getModeId())
              .append("  软件执行id=").append(dm.getSfIndex()).append("\n");
        }
        sb.append("\n");
    }
    sb.append("【刷新率显示开关诊断】\n");
    try {
        Process p3 = Runtime.getRuntime().exec("su");
        java.io.DataOutputStream os4 = new java.io.DataOutputStream(p3.getOutputStream());
        os4.writeBytes(
            "echo '=global='; settings list global | grep -i refresh\n" +
            "echo '=system='; settings list system | grep -i refresh\n" +
            "echo '=secure='; settings list secure | grep -i refresh\n" +
            "echo '=props='; getprop | grep -i 'refresh\\|fps\\|overlay'\n" +
            "exit\n"
        );
        os4.flush();
        java.io.BufferedReader r3 = new java.io.BufferedReader(new java.io.InputStreamReader(p3.getInputStream()));
        String line3; while ((line3 = r3.readLine()) != null) sb.append(line3).append("\n");
        p3.waitFor();
    } catch (Exception e4) { sb.append("诊断失败(可能无Root): ").append(e4.getMessage()).append("\n"); }
    sb.append("\n");
    sb.append("【Raw dumpsys 档位列表】\n");
    if (!rooted) {
        sb.append("（需要Root权限才能读取）\n");
    } else {
        Process p = null;
        DataOutputStream os = null; BufferedReader r = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("dumpsys display | grep 'DisplayModeRecord'\nexit\n");
            os.flush();
            r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int count = 0;
            while ((line = r.readLine()) != null) {
                sb.append(line.trim()).append("\n");
                count++;
            }
            if (count == 0) sb.append("（无输出）\n");
        } catch (Exception e) {
            sb.append("提取错误: ").append(e.getMessage()).append("\n");
        } finally {
            try { if(os!=null)os.close();
            }catch(Exception ignored){}
            try { if(r!=null)r.close();
            }catch(Exception ignored){}
            try { if(p!=null)p.destroy();
            }catch(Exception ignored){}
        }
    }
    sb.append("\n=====\n");
    return sb.toString();
}
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/util/ShizukuUtils.java << 'EOF'
package com.yxlanyu.refreshrate.util;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.*;
import rikka.shizuku.Shizuku;
public class ShizukuUtils {
    private static final String TAG = "ShizukuUtils";
    public static final int REQUEST_CODE = 1001;
    public static boolean isAvailable() {
        try { return Shizuku.pingBinder();
        }
        catch (Exception e) { return false;
        }
    }
    public static boolean hasPermission() {
        try {
            if (Shizuku.isPreV11()) return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) { return false;
        }
    }
    public static void requestPermission() {
        try { Shizuku.requestPermission(REQUEST_CODE);
        }
        catch (Exception e) { Log.e(TAG, "requestPermission: " + e.getMessage());
        }
    }
    public static boolean setDisplayMode(int w, int h, int hz, int sfIndex) {
        if (!isAvailable() || !hasPermission()) return false;
        execShizuku(RootUtils.buildSwitchScript(w, h, hz, sfIndex, null));
        try {
            if (sfIndex >= 0 && hz > 0) {
                Thread.sleep(180);
                int landedSf = activeSfIndexShizuku();
                if (landedSf >= 0 && landedSf != sfIndex) {
                    int correct = 2 * sfIndex - landedSf;
                    if (correct >= 0 && correct < 64)
                        execShizuku("service call SurfaceFlinger 1035 i32 " + correct + " >/dev/null 2>&1; settings put system peak_refresh_rate " + hz + ".0; settings put system min_refresh_rate " + hz + ".0");
                }
            }
        } catch (Exception ignored) {}
        return true;
    }
    private static int activeSfIndexShizuku() {
        try {
            String out = execAndRead("dumpsys display");
            if (out == null) return -1;
            java.util.regex.Matcher a = java.util.regex.Pattern.compile("mActiveModeId=(\\d+)").matcher(out);
            if (!a.find()) return -1;
            int activeId = Integer.parseInt(a.group(1));
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("id=(\\d+),\\s*width=").matcher(out);
            int sf = 0;
            while (m.find()) { if (Integer.parseInt(m.group(1)) == activeId) return sf; sf++; }
        } catch (Exception ignored) {}
        return -1;
    }
    public static boolean restoreAdaptive(int minHz, int maxHz) {
        if (!isAvailable() || !hasPermission()) return false;
        StringBuilder sb = new StringBuilder();
        sb.append("cmd display clear-user-preferred-display-mode 2>/dev/null; ");
        if (minHz > 0) sb.append("settings put system min_refresh_rate ").append(minHz).append(".0; ");
        if (maxHz > 0) sb.append("settings put system peak_refresh_rate ").append(maxHz).append(".0");
        return execShizuku(sb.toString());
    }
    public static boolean setNativeRefreshOverlay(boolean on) {
        if (!isAvailable() || !hasPermission()) return false;
        return execShizuku("service call SurfaceFlinger 1034 i32 " + (on ? 1 : 0));
    }
    public static String execAndRead(String cmd) {
        if (!isAvailable() || !hasPermission()) return "";
        try {
            Process p = execShizukuProcess(cmd);
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
            p.waitFor();
            return sb.toString().trim();
        } catch (Exception e) {
            Log.e(TAG, "execAndRead: " + e.getMessage());
            return "";
        }
    }
    private static boolean execShizuku(String cmd) {
        try {
            Process p = execShizukuProcess(cmd);
            int code = p.waitFor();
            Log.d(TAG, "execShizuku exit=" + code + " cmd=" + cmd);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "execShizuku: " + e.getMessage());
            return false;
        }
    }
    private static Process execShizukuProcess(String cmd) throws Exception {
        java.lang.reflect.Method method = Shizuku.class.getDeclaredMethod(
            "newProcess", String[].class, String[].class, String.class);
        method.setAccessible(true);
        return (Process) method.invoke(null, new String[]{"sh", "-c", cmd}, null, null);
    }
}
EOF



cat > app/src/main/java/com/yxlanyu/refreshrate/util/AccessibilityUtils.java << 'EOF'
package com.yxlanyu.refreshrate.util;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import com.yxlanyu.refreshrate.service.KeepAliveAccessibilityService;
import java.util.Locale;
public class AccessibilityUtils {
    public static boolean isKeepAliveServiceEnabled(Context context) {
        if (context == null) return false;
        try {
            String enabledServices = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (TextUtils.isEmpty(enabledServices)) return false;
            ComponentName target = new ComponentName(context, KeepAliveAccessibilityService.class);
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
            splitter.setString(enabledServices);
            while (splitter.hasNext()) {
                String item = splitter.next();
                if (TextUtils.isEmpty(item)) continue;
                ComponentName enabled = ComponentName.unflattenFromString(item);
                if (target.equals(enabled)) return true;
                String normalized = item.trim().toLowerCase(Locale.ROOT);
                String pkg = context.getPackageName().toLowerCase(Locale.ROOT);
                String full = (context.getPackageName() + "/" + KeepAliveAccessibilityService.class.getName()).toLowerCase(Locale.ROOT);
                String shortName = (context.getPackageName() + "/.service.KeepAliveAccessibilityService").toLowerCase(Locale.ROOT);
                if (normalized.equals(full) || normalized.equals(shortName)) return true;
                if (normalized.startsWith(pkg + "/") && normalized.endsWith(".keepaliveaccessibilityservice")) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
EOF


cat > app/src/main/java/com/yxlanyu/refreshrate/model/AppInfo.java << 'EOF'
package com.yxlanyu.refreshrate.model;
import android.graphics.drawable.Drawable;
public class AppInfo {
    private final String name;
    private final String packageName;
    private final Drawable icon;
    private final boolean systemApp;
    private final int userId;
    public AppInfo(String name, String packageName, Drawable icon, boolean systemApp) {
        this(name, packageName, icon, systemApp, 0);
    }
    public AppInfo(String name, String packageName, Drawable icon, boolean systemApp, int userId) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.systemApp = systemApp;
        this.userId = userId;
    }
    public String getName() { return name; }
    public String getPackageName() { return packageName; }
    public Drawable getIcon() { return icon; }
    public boolean isSystemApp() { return systemApp; }
    public int getUserId() { return userId; }
    public String getEffectivePkg() {
        return userId > 0 ? packageName + ":u" + userId : packageName;
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/adapter/AppListAdapter.java << 'EOF'
package com.yxlanyu.refreshrate.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.AppInfo;
import java.util.ArrayList;
import java.util.List;
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.VH> {
    public interface OnAppClick { void onClick(AppInfo app); }
    private final Context context;
    private final OnAppClick listener;
    private List<AppInfo> apps = new ArrayList<>();
    public AppListAdapter(Context context, OnAppClick listener) {
        this.context = context;
        this.listener = listener;
    }
    public void submit(List<AppInfo> newApps) {
        apps = newApps == null ? new ArrayList<>() : newApps;
        notifyDataSetChanged();
    }
    @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.item_app_entry, parent, false));
    }
    @Override public void onBindViewHolder(VH h, int pos) {
        AppInfo app = apps.get(pos);
        h.icon.setImageDrawable(app.getIcon());
        h.name.setText(app.getName());
        h.pkg.setText(app.getPackageName());
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(app);
        });
    }
    @Override public int getItemCount() { return apps == null ? 0 : apps.size(); }
    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, pkg;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.iv_app_icon);
            name = v.findViewById(R.id.tv_app_name);
            pkg = v.findViewById(R.id.tv_app_package);
        }
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/AppListActivity.java << 'EOF'
package com.yxlanyu.refreshrate;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yxlanyu.refreshrate.adapter.AppListAdapter;
import com.yxlanyu.refreshrate.model.AppInfo;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class AppListActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private SharedPreferences prefs;
    private final List<AppInfo> allApps = new ArrayList<>();
    private AppListAdapter adapter;
    private EditText etSearch;
    private TextView tvHint;
    private TextView tvEmpty;
    private boolean showSystemApps = false;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        LanguageUtils.applyLanguage(this);
        setContentView(R.layout.activity_app_list);
        prefs = getSharedPreferences("s", MODE_PRIVATE);
        showSystemApps = prefs.getBoolean("show_system_apps_in_list", false);
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnSearch = findViewById(R.id.btn_search);
        ImageView btnMore = findViewById(R.id.btn_more);
        etSearch = findViewById(R.id.et_search);
        tvHint = findViewById(R.id.tv_app_list_hint);
        tvEmpty = findViewById(R.id.tv_empty);
        btnBack.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> {
            etSearch.setVisibility(etSearch.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            if (etSearch.getVisibility() == View.VISIBLE) etSearch.requestFocus();
        });
        btnMore.setOnClickListener(this::showMoreMenu);
        RecyclerView rv = findViewById(R.id.recycler_apps);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(this, app -> {
            Intent i = new Intent(this, AppRefreshConfigActivity.class);
            i.putExtra("pkg", app.getEffectivePkg());
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        rv.setAdapter(adapter);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterApps(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        showAppListPermissionHint();
        loadApps();
    }
    private void showAppListPermissionHint() {
        if (prefs.getBoolean("app_list_permission_hint_shown", false)) return;
        prefs.edit().putBoolean("app_list_permission_hint_shown", true).apply();
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_list_permission_title)
                .setMessage(R.string.app_list_permission_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
    private void showMoreMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        android.view.MenuItem item = popup.getMenu().add(R.string.show_system_apps);
        item.setCheckable(true);
        item.setChecked(showSystemApps);
        popup.setOnMenuItemClickListener(menuItem -> {
            showSystemApps = !showSystemApps;
            prefs.edit().putBoolean("show_system_apps_in_list", showSystemApps).apply();
            updateHint();
            filterApps();
            return true;
        });
        popup.show();
    }
    private void loadApps() {
        new Thread(() -> {
            List<AppInfo> result = new ArrayList<>();
            PackageManager pm = getPackageManager();
            try {
                List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                java.util.Set<String> primaryPkgs = new java.util.HashSet<>();
                for (ApplicationInfo ai : installed) {
                    String pkg = ai.packageName;
                    if (pkg == null || pkg.equals(getPackageName())) continue;
                    primaryPkgs.add(pkg);
                    boolean system = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;
                    String label = String.valueOf(pm.getApplicationLabel(ai));
                    result.add(new AppInfo(label, pkg, pm.getApplicationIcon(ai), system, 0));
                }
                try {
                    android.os.UserManager um = (android.os.UserManager) getSystemService(android.content.Context.USER_SERVICE);
                    List<android.os.UserHandle> profiles = um.getUserProfiles();
                    for (android.os.UserHandle uh : profiles) {
                        int uid = 0;
                        try {
                            java.lang.reflect.Method getId = android.os.UserHandle.class.getDeclaredMethod("getIdentifier");
                            uid = (int) getId.invoke(uh);
                        } catch (Exception ignored) {}
                        if (uid == 0) continue;
                        try {
                            java.lang.reflect.Method getAppsAsUser = PackageManager.class.getMethod(
                                "getInstalledApplicationsAsUser", int.class, int.class);
                            @SuppressWarnings("unchecked")
                            List<ApplicationInfo> cloneApps = (List<ApplicationInfo>) getAppsAsUser.invoke(pm, PackageManager.GET_META_DATA, uid);
                            if (cloneApps == null) continue;
                            for (ApplicationInfo ai : cloneApps) {
                                String pkg = ai.packageName;
                                if (pkg == null || !primaryPkgs.contains(pkg)) continue;
                                if (pkg.equals(getPackageName())) continue;
                                boolean system = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;
                                String label = String.valueOf(pm.getApplicationLabel(ai)) + " (分身)";
                                result.add(new AppInfo(label, pkg, pm.getApplicationIcon(ai), system, uid));
                            }
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
                Collator collator = Collator.getInstance(Locale.getDefault());
                result.sort((a, b) -> {
                    int c = collator.compare(a.getPackageName(), b.getPackageName());
                    return c != 0 ? c : Integer.compare(a.getUserId(), b.getUserId());
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.app_list_load_failed, Toast.LENGTH_SHORT).show());
            }
            runOnUiThread(() -> {
                allApps.clear();
                allApps.addAll(result);
                updateHint();
                filterApps();
            });
        }).start();
    }
    private void updateHint() {
        if (tvHint == null) return;
        tvHint.setText(showSystemApps ? R.string.app_list_all_hint : R.string.app_list_third_party_hint);
    }
    private void filterApps() {
        String q = etSearch == null ? "" : etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<AppInfo> shown = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (!showSystemApps && app.isSystemApp()) continue;
            if (!q.isEmpty()) {
                String n = app.getName().toLowerCase(Locale.ROOT);
                String p = app.getPackageName().toLowerCase(Locale.ROOT);
                if (!n.contains(q) && !p.contains(q)) continue;
            }
            shown.add(app);
        }
        adapter.submit(shown);
        if (tvEmpty != null) tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }
    @Override public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/AppRefreshConfigActivity.java << 'EOF'
package com.yxlanyu.refreshrate;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
import com.yxlanyu.refreshrate.util.AccessibilityUtils;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
public class AppRefreshConfigActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private SharedPreferences prefs;
    private String pkg;
    private View targetContainer;
    private Spinner spinnerRes;
    private Spinner spinnerRate;
    private final List<Integer>[] rateHolder = new List[]{new ArrayList<>()};
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        LanguageUtils.applyLanguage(this);
        setContentView(R.layout.activity_app_refresh_config);
        prefs = getSharedPreferences("s", MODE_PRIVATE);
        pkg = getIntent().getStringExtra("pkg");
        if (pkg == null || pkg.trim().isEmpty()) {
            finish();
            return;
        }
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        targetContainer = findViewById(R.id.ll_app_target_container);
        spinnerRes = findViewById(R.id.spinner_app_resolution);
        spinnerRate = findViewById(R.id.spinner_app_rate);
        bindAppHeader();
        setupTargetSelectors();
        setupSwitch();
    }
    private void bindAppHeader() {
        ImageView icon = findViewById(R.id.iv_app_icon);
        TextView name = findViewById(R.id.tv_app_name);
        TextView pkgView = findViewById(R.id.tv_app_package);
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            icon.setImageDrawable(pm.getApplicationIcon(ai));
            name.setText(pm.getApplicationLabel(ai));
            pkgView.setText(pkg);
        } catch (Exception e) {
            name.setText(pkg);
            pkgView.setText(pkg);
        }
    }
    private void setupSwitch() {
        SwitchCompat sw = findViewById(R.id.switch_single_app_refresh);
        boolean enabled = prefs.getBoolean(key("enabled"), false);
        sw.setChecked(enabled);
        showTargetContainer(enabled);
        sw.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                if (!prefs.getBoolean("custom_app_refresh", false)) {
                    Toast.makeText(this, R.string.custom_app_master_required, Toast.LENGTH_SHORT).show();
                    sw.setChecked(false);
                    return;
                }
                if (!isAccessibilityServiceEnabled()) {
                    Toast.makeText(this, R.string.accessibility_required, Toast.LENGTH_LONG).show();
                    try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
                    catch (Exception e) { Toast.makeText(this, R.string.accessibility_open_failed, Toast.LENGTH_SHORT).show(); }
                    sw.setChecked(false);
                    return;
                }
                if (prefs.getString("auth_mode", "").isEmpty()) {
                    Toast.makeText(this, R.string.no_any_permission, Toast.LENGTH_SHORT).show();
                    sw.setChecked(false);
                    return;
                }
            }
            prefs.edit().putBoolean(key("enabled"), checked).apply();
            showTargetContainer(checked);
            if (checked) saveCurrentSelections();
        });
    }
    private void setupTargetSelectors() {
        List<DisplayMode> allModes = AutoOverclockManager.getSupportedModes(this);
        LinkedHashMap<String, List<DisplayMode>> resMap = new LinkedHashMap<>();
        for (DisplayMode m : allModes) {
            String k = m.getWidth() + "x" + m.getHeight();
            resMap.computeIfAbsent(k, kk -> new ArrayList<>()).add(m);
        }
        List<String> resKeys = new ArrayList<>(resMap.keySet());
        resKeys.sort((a, b) -> {
            try {
                String[] pa = a.split("x"), pb = b.split("x");
                return Integer.parseInt(pb[0]) * Integer.parseInt(pb[1]) - Integer.parseInt(pa[0]) * Integer.parseInt(pa[1]);
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });
        List<String> resLabels = new ArrayList<>();
        for (String k : resKeys) resLabels.add(k.replace("x", "×"));
        ArrayAdapter<String> resAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, resLabels);
        resAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRes.setAdapter(resAdapter);
        String savedRes = prefs.getString(key("res"), "");
        int savedResIdx = resKeys.indexOf(savedRes);
        if (savedResIdx < 0) savedResIdx = 0;
        if (!resKeys.isEmpty()) {
            spinnerRes.setSelection(savedResIdx);
            if (savedRes.isEmpty()) prefs.edit().putString(key("res"), resKeys.get(0)).apply();
        }
        Runnable populateRates = () -> {
            int resIdx = spinnerRes.getSelectedItemPosition();
            if (resIdx < 0 || resIdx >= resKeys.size()) return;
            String rk = resKeys.get(resIdx);
            List<DisplayMode> modes = resMap.get(rk);
            if (modes == null) return;
            modes.sort((a, b) -> Float.compare(b.getRefreshRate(), a.getRefreshRate()));
            List<String> rateLabels = new ArrayList<>();
            rateHolder[0] = new ArrayList<>();
            for (DisplayMode m : modes) {
                rateLabels.add(m.getRateInt() + " Hz");
                rateHolder[0].add(m.getRateInt());
            }
            ArrayAdapter<String> rateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rateLabels);
            rateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRate.setAdapter(rateAdapter);
            int savedHz = prefs.getInt(key("hz"), -1);
            int idx = rateHolder[0].indexOf(savedHz);
            if (!rateHolder[0].isEmpty()) {
                spinnerRate.setSelection(idx >= 0 ? idx : 0);
                if (savedHz < 0) prefs.edit().putInt(key("hz"), rateHolder[0].get(0)).apply();
            }
        };
        populateRates.run();
        spinnerRes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position < 0 || position >= resKeys.size()) return;
                prefs.edit().putString(key("res"), resKeys.get(position)).apply();
                populateRates.run();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        spinnerRate.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position < 0 || position >= rateHolder[0].size()) return;
                prefs.edit().putInt(key("hz"), rateHolder[0].get(position)).apply();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
    private void saveCurrentSelections() {
        if (spinnerRes == null || spinnerRate == null) return;
        int rIdx = spinnerRes.getSelectedItemPosition();
        int hIdx = spinnerRate.getSelectedItemPosition();
        String resVal = prefs.getString(key("res"), "");
        int    hzVal  = prefs.getInt(key("hz"), -1);
        if (resVal.isEmpty() && spinnerRes.getAdapter() != null && rIdx >= 0) {
            String label = (String) spinnerRes.getAdapter().getItem(rIdx);
            if (label != null) prefs.edit().putString(key("res"), label.replace("×", "x")).apply();
        }
        if (hzVal < 0 && spinnerRate.getAdapter() != null && hIdx >= 0) {
            String label = (String) spinnerRate.getAdapter().getItem(hIdx);
            if (label != null) {
                try { prefs.edit().putInt(key("hz"), Integer.parseInt(label.replace(" Hz", "").trim())).apply(); } catch (Exception ignored) {}
            }
        }
    }
    private boolean isAccessibilityServiceEnabled() {
        return AccessibilityUtils.isKeepAliveServiceEnabled(this);
    }
    private String key(String suffix) {
        return "app_refresh_" + suffix + "_" + pkg;
    }
    private void showTargetContainer(boolean show) {
        if (targetContainer != null) targetContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    @Override public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/service/KeepAliveAccessibilityService.java << 'EOF'
package com.yxlanyu.refreshrate.service;
import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import androidx.core.app.NotificationCompat;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
import com.yxlanyu.refreshrate.util.RootUtils;
import com.yxlanyu.refreshrate.util.ShizukuUtils;
import java.util.List;
public class KeepAliveAccessibilityService extends AccessibilityService {
    private static final String TAG = "KeepAliveA11y";
    private static final String CUSTOM_CHANNEL_ID = "custom_app_channel";
    private static final int CUSTOM_NOTIF_ID = 1002;
    private String currentFgPackage = "";
    private volatile String pendingFgPackage = "";
    private volatile String lastAppliedConfig = "";  // "effectivePkg@res@hz"
    private volatile String lastRealPkg = "";     
    private android.os.Handler fgHandler;
    private Runnable pendingApplyRunnable;
    private SharedPreferences servicePrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        fgHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        Log.d(TAG, "无障碍服务已连接");
        createCustomChannel();
        servicePrefs = getSharedPreferences("s", MODE_PRIVATE);
        prefListener = (sp, key) -> {
            if (!"custom_app_refresh".equals(key)) return;
            if (sp.getBoolean("custom_app_refresh", false)) {
                if (lastRealPkg != null && !lastRealPkg.isEmpty()) updatePersistentNotification(lastRealPkg);
                else postWaitingNotification();
            } else {
                cancelCustomNotification();
            }
        };
        servicePrefs.registerOnSharedPreferenceChangeListener(prefListener);
        if (servicePrefs.getBoolean("custom_app_refresh", false)) postWaitingNotification();
        checkAndRestartService();
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                type != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return;
        String pkg = getTopForegroundPackage(event);
        if (pkg == null || pkg.isEmpty()) return;
        if (pkg.equals(getPackageName()) || pkg.equals("android")) return;
        scheduleForegroundApply(pkg);
    }
    private String getTopForegroundPackage(AccessibilityEvent event) {
        try {
            java.util.List<android.view.accessibility.AccessibilityWindowInfo> windows = getWindows();
            if (windows != null) {
                for (android.view.accessibility.AccessibilityWindowInfo w : windows) {
                    if (w != null && w.isFocused() &&
                            w.getType() == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION) {
                        android.view.accessibility.AccessibilityNodeInfo root = w.getRoot();
                        if (root != null) {
                            CharSequence p = root.getPackageName();
                            root.recycle();
                            if (p != null && p.length() > 0) return p.toString();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        CharSequence p = event.getPackageName();
        return p != null ? p.toString() : null;
    }
    private void scheduleForegroundApply(String pkg) {
        pendingFgPackage = pkg;
        final String finalPkg = pkg;
        new Thread(() -> {
            if (!finalPkg.equals(pendingFgPackage)) return;
            applyForPackage(finalPkg);
            updatePersistentNotification(finalPkg);
        }).start();
    }
    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务中断");
        checkAndRestartService();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "无障碍服务销毁");
        try { if (servicePrefs != null && prefListener != null) servicePrefs.unregisterOnSharedPreferenceChangeListener(prefListener); } catch (Exception ignored) {}
        checkAndRestartService();
    }
    private synchronized void applyForPackage(String basePkg) {
        if (basePkg == null || basePkg.isEmpty()) return;
        if (basePkg.equals("android") || basePkg.equals(getPackageName())) return;
        SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
        if (!prefs.getBoolean("custom_app_refresh", false)) return;
        String authMode = prefs.getString("auth_mode", "");
        if (authMode == null || authMode.isEmpty()) return;
        String effectivePkg = resolveEffectivePkg(prefs, basePkg);
        boolean enabled = prefs.getBoolean("app_refresh_enabled_" + effectivePkg, false);
        if (!enabled) return;
        String res = prefs.getString("app_refresh_res_" + effectivePkg, "");
        int hz = prefs.getInt("app_refresh_hz_" + effectivePkg, -1);
        if (res == null || res.isEmpty() || hz <= 0) return;
        String configKey = effectivePkg + "@" + res + "@" + hz;
        if (configKey.equals(lastAppliedConfig)) return;
        currentFgPackage = basePkg;
        lastAppliedConfig = configKey;
        Log.d(TAG, "自定义刷新率切换: " + effectivePkg + " → " + res + " @ " + hz + "Hz");
        applyDisplayTarget(authMode, res, hz);
    }
    private void updatePersistentNotification(String basePkg) {
        try {
            SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
            if (!prefs.getBoolean("custom_app_refresh", false)) { cancelCustomNotification(); return; }
            if (basePkg == null || basePkg.isEmpty()) return;
            if (isIgnoredForNotification(prefs, basePkg)) return;
            lastRealPkg = basePkg;
            String effectivePkg = resolveEffectivePkg(prefs, basePkg);
            boolean enabled = prefs.getBoolean("app_refresh_enabled_" + effectivePkg, false);
            String setRes = prefs.getString("app_refresh_res_" + effectivePkg, "");
            int setHz = prefs.getInt("app_refresh_hz_" + effectivePkg, -1);
            int[] cur = getCurrentWHRate();
            String curRes = cur[0] > 0 ? (cur[0] + "×" + cur[1]) : "?";
            int curHz = cur[2];
            postCustomNotification(basePkg, enabled, setRes, setHz, curRes, curHz);
        } catch (Exception e) {
            Log.e(TAG, "updatePersistentNotification: " + e.getMessage());
        }
    }
    private boolean isIgnoredForNotification(SharedPreferences prefs, String pkg) {
        if (pkg.equals("android") || pkg.equals(getPackageName())) return true;
        if (pkg.equals("com.android.systemui")) return true;
        if (prefs.getBoolean("app_refresh_enabled_" + pkg, false)) return false;
        try {
            if (pkg.equals(getLauncherPackage())) return true;
            if (getPackageManager().getLaunchIntentForPackage(pkg) == null) return true;
        } catch (Exception ignored) {}
        return false;
    }
    private String getLauncherPackage() {
        try {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            android.content.pm.ResolveInfo ri = getPackageManager()
                    .resolveActivity(home, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
            if (ri != null && ri.activityInfo != null) return ri.activityInfo.packageName;
        } catch (Exception ignored) {}
        return "";
    }
    private int[] getCurrentWHRate() {
        try {
            android.hardware.display.DisplayManager dm =
                    (android.hardware.display.DisplayManager) getSystemService(DISPLAY_SERVICE);
            android.view.Display d = dm.getDisplay(android.view.Display.DEFAULT_DISPLAY);
            android.view.Display.Mode m = d.getMode();
            return new int[]{ m.getPhysicalWidth(), m.getPhysicalHeight(), Math.round(d.getRefreshRate()) };
        } catch (Exception e) { return new int[]{0, 0, 0}; }
    }
    private void createCustomChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Context lc = getLocalizedCtx();
                NotificationChannel ch = new NotificationChannel(
                        CUSTOM_CHANNEL_ID,
                        lc.getString(R.string.custom_notif_channel_name),
                        NotificationManager.IMPORTANCE_LOW);
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null) nm.createNotificationChannel(ch);
            } catch (Exception ignored) {}
        }
    }
    private Context getLocalizedCtx() {
        try {
            SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
            String lang = prefs.getString("language", "zh");
            java.util.Locale locale = "zh-rTW".equals(lang) ? new java.util.Locale("zh", "TW") : new java.util.Locale(lang);
            android.content.res.Configuration config = new android.content.res.Configuration(getResources().getConfiguration());
            config.setLocale(locale);
            return createConfigurationContext(config);
        } catch (Exception e) { return this; }
    }
    private void postCustomNotification(String pkg, boolean enabled, String setRes, int setHz,
                                        String curRes, int curHz) {
        try {
            Context lc = getLocalizedCtx();
            String appLabel = pkg;
            try {
                android.content.pm.PackageManager pm = getPackageManager();
                appLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
            } catch (Exception ignored) {}
            Intent openIntent = new Intent(this, com.yxlanyu.refreshrate.MainActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            int of = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 31) of |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent openPi = PendingIntent.getActivity(this, 3, openIntent, of);
            Intent stopIntent = new Intent(this, CustomRefreshActionReceiver.class);
            stopIntent.setAction("STOP_CUSTOM_REFRESH");
            int sf = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 31) sf |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent stopPi = PendingIntent.getBroadcast(this, 4, stopIntent, sf);
            String statusLine = enabled
                    ? lc.getString(R.string.custom_notif_status_on)
                    : lc.getString(R.string.custom_notif_status_off);
            String curLine = lc.getString(R.string.custom_notif_current_format, curRes, curHz);
            String title = appLabel + "  " + statusLine;
            StringBuilder big = new StringBuilder();
            if (enabled && setRes != null && !setRes.isEmpty() && setHz > 0) {
                big.append(lc.getString(R.string.custom_notif_set_format, setRes, setHz)).append("\n");
            }
            big.append(curLine);
            Notification n = new NotificationCompat.Builder(this, CUSTOM_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(curLine)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(big.toString()).setBigContentTitle(title))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(openPi)
                    .addAction(android.R.drawable.ic_media_pause, lc.getString(R.string.custom_notif_stop), stopPi)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .build();
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.notify(CUSTOM_NOTIF_ID, n);
        } catch (Exception e) {
            Log.e(TAG, "postCustomNotification: " + e.getMessage());
        }
    }
    private void cancelCustomNotification() {
        try {
            lastAppliedConfig = "";
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel(CUSTOM_NOTIF_ID);
        } catch (Exception ignored) {}
    }
    private void postWaitingNotification() {
        try {
            Context lc = getLocalizedCtx();
            Intent openIntent = new Intent(this, com.yxlanyu.refreshrate.MainActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            int of = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 31) of |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent openPi = PendingIntent.getActivity(this, 3, openIntent, of);
            Intent stopIntent = new Intent(this, CustomRefreshActionReceiver.class);
            stopIntent.setAction("STOP_CUSTOM_REFRESH");
            int sf = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 31) sf |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent stopPi = PendingIntent.getBroadcast(this, 4, stopIntent, sf);
            String title = lc.getString(R.string.custom_notif_waiting_title);
            String text = lc.getString(R.string.custom_notif_waiting_text);
            Notification n = new NotificationCompat.Builder(this, CUSTOM_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(openPi)
                    .addAction(android.R.drawable.ic_media_pause, lc.getString(R.string.custom_notif_stop), stopPi)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .build();
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.notify(CUSTOM_NOTIF_ID, n);
        } catch (Exception ignored) {}
    }
    private String resolveEffectivePkg(SharedPreferences prefs, String basePkg) {
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<android.app.ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
            if (procs != null) {
                for (android.app.ActivityManager.RunningAppProcessInfo proc : procs) {
                    if (proc.importance != android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) continue;
                    if (proc.pkgList == null) continue;
                    boolean matches = false;
                    for (String p : proc.pkgList) {
                        if (p.equals(basePkg)) { matches = true; break; }
                    }
                    if (!matches) continue;
                    int userId = proc.uid / 100000;
                    if (userId > 0) {
                        String cloneKey = basePkg + ":u" + userId;
                        if (prefs.getBoolean("app_refresh_enabled_" + cloneKey, false)) {
                            return cloneKey;
                        }
                    }
                    return basePkg;
                }
            }
        } catch (Exception ignored) {}
        return basePkg;
    }
    private void applyDisplayTarget(String authMode, String res, int hz) {
        try {
            String[] wh = res.split("x");
            if (wh.length != 2) return;
            int targetW = Integer.parseInt(wh[0]);
            int targetH = Integer.parseInt(wh[1]);
            List<DisplayMode> modes = AutoOverclockManager.getSupportedModes(this);
            DisplayMode target = null;
            for (DisplayMode m : modes) {
                if (m.getWidth() == targetW && m.getHeight() == targetH && m.getRateInt() == hz) {
                    target = m;
                    break;
                }
            }
            if (target == null) {
                for (DisplayMode m : modes) {
                    if (m.getWidth() == targetW && m.getHeight() == targetH) {
                        if (target == null || Math.abs(m.getRateInt() - hz) < Math.abs(target.getRateInt() - hz)) {
                            target = m;
                        }
                    }
                }
            }
            if (target == null) return;
            if ("root".equals(authMode)) {
                RootUtils.setDisplayMode(target.getWidth(), target.getHeight(), target.getRateInt(), target.getSfIndex());
            } else if ("shizuku".equals(authMode)) {
                ShizukuUtils.setDisplayMode(target.getWidth(), target.getHeight(), target.getRateInt(), target.getSfIndex());
            }
            Log.d(TAG, "应用刷新率已切换: " + res + " @ " + hz + "Hz");
        } catch (Exception e) {
            Log.e(TAG, "应用刷新率切换失败: " + e.getMessage());
        }
    }
    private void checkAndRestartService() {
        SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
        if (!prefs.getBoolean("auto_overclock", false)) return;
        String authMode = prefs.getString("auth_mode", "");
        String ocRes    = prefs.getString("oc_target_res", "");
        int    ocHz     = prefs.getInt("oc_target_hz", -1);
        if (authMode.isEmpty() || ocRes.isEmpty() || ocHz < 0) return;
        String[] wh = ocRes.split("x");
        if (wh.length < 2) return;
        try {
            int tw = Integer.parseInt(wh[0]);
            int th = Integer.parseInt(wh[1]);
            Intent si = new Intent(this, OverclockService.class);
            si.putExtra("auth_mode", authMode);
            si.putExtra("targetW", tw);
            si.putExtra("targetH", th);
            si.putExtra("targetHz", ocHz);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(si);
            else
                startService(si);
        } catch (Exception e) {
            Log.e(TAG, "重启服务失败: " + e.getMessage());
        }
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/service/OverclockService.java << 'EOF'
package com.yxlanyu.refreshrate.service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
public class OverclockService extends Service {
    private static final String CHANNEL_ID = "overclock_channel";
    private static final int NOTIFICATION_ID = 1001;
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            AutoOverclockManager.stopService(this);
            return START_NOT_STICKY;
        }
        Context lc = getLocalizedContext(this);
        startForeground(NOTIFICATION_ID, buildNotification());
        String authMode = intent != null ? intent.getStringExtra("auth_mode") : "";
        int targetW = intent != null ? intent.getIntExtra("targetW", 0) : 0;
        int targetH = intent != null ? intent.getIntExtra("targetH", 0) : 0;
        int targetHz = intent != null ? intent.getIntExtra("targetHz", 0) : 0;
        AutoOverclockManager.start(this, authMode, targetW, targetH, targetHz);
        return START_STICKY;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        android.app.PendingIntent restartPi;
        Intent restartIntent = new Intent(getApplicationContext(), OverclockRestartReceiver.class);
        int flags = android.app.PendingIntent.FLAG_ONE_SHOT;
        if (android.os.Build.VERSION.SDK_INT >= 23)
            flags |= android.app.PendingIntent.FLAG_IMMUTABLE;
        restartPi = android.app.PendingIntent.getBroadcast(getApplicationContext(), 1, restartIntent, flags);
        android.app.AlarmManager am = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
        if (am != null)
            am.set(android.app.AlarmManager.ELAPSED_REALTIME,
                    android.os.SystemClock.elapsedRealtime() + 1000, restartPi);
        super.onTaskRemoved(rootIntent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        AutoOverclockManager.stop();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private static Context getLocalizedContext(Context ctx) {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("s", Context.MODE_PRIVATE);
            String lang = prefs.getString("language", "zh");
            java.util.Locale locale = "zh-rTW".equals(lang) ? new java.util.Locale("zh", "TW") : new java.util.Locale(lang);
            android.content.res.Configuration config = new android.content.res.Configuration(ctx.getResources().getConfiguration());
            config.setLocale(locale);
            return ctx.createConfigurationContext(config);
        } catch (Exception e) { return ctx;
        }
    }
    private void createNotificationChannel() {
    Context lc = getLocalizedContext(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    lc.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    private Notification buildNotification() {
    Context lc = getLocalizedContext(this);
    Intent stopIntent = new Intent(this, OverclockService.class);
    stopIntent.setAction("STOP");
    int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= 31) pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
    PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, pendingFlags);
    Intent openIntent = new Intent(this, com.yxlanyu.refreshrate.MainActivity.class);
    openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    int openFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= 31) openFlags |= PendingIntent.FLAG_IMMUTABLE;
    PendingIntent openPendingIntent = PendingIntent.getActivity(this, 2, openIntent, openFlags);
    return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(lc.getString(R.string.notification_title))
            .setContentText(AutoOverclockManager.getLastLog())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, lc.getString(R.string.notification_stop), stopPendingIntent)
            .setOngoing(true)
            .build();
    }
    public static void updateNotification(Context context) {
    Context lc = getLocalizedContext(context);
    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if (nm == null) return;
    Intent stopIntent = new Intent(context, OverclockService.class);
    stopIntent.setAction("STOP");
    int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= 31) pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
    PendingIntent stopPendingIntent = PendingIntent.getService(context, 0, stopIntent, pendingFlags);
    Intent openIntent = new Intent(context, com.yxlanyu.refreshrate.MainActivity.class);
    openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    int openFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= 31) openFlags |= PendingIntent.FLAG_IMMUTABLE;
    PendingIntent openPendingIntent = PendingIntent.getActivity(context, 2, openIntent, openFlags);
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(lc.getString(R.string.notification_title))
            .setContentText(AutoOverclockManager.getLastLog())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, lc.getString(R.string.notification_stop), stopPendingIntent)
            .setOngoing(true)
            .build();
    nm.notify(NOTIFICATION_ID, notification);
}
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/service/OverclockRestartReceiver.java << 'EOF'
package com.yxlanyu.refreshrate.service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
public class OverclockRestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("s", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("auto_overclock", false)) return;
        String authMode = prefs.getString("auth_mode", "");
        String ocRes    = prefs.getString("oc_target_res", "");
        int    ocHz     = prefs.getInt("oc_target_hz", -1);
        if (authMode.isEmpty() || ocRes.isEmpty() || ocHz < 0) return;
        String[] wh = ocRes.split("x");
        if (wh.length < 2) return;
        try {
            int tw = Integer.parseInt(wh[0]);
            int th = Integer.parseInt(wh[1]);
            Intent si = new Intent(context, OverclockService.class);
            si.putExtra("auth_mode", authMode);
            si.putExtra("targetW", tw);
            si.putExtra("targetH", th);
            si.putExtra("targetHz", ocHz);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(si);
            else
                context.startService(si);
        } catch (Exception ignored) {}
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/service/BootReceiver.java << 'EOF'
package com.yxlanyu.refreshrate.service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        new OverclockRestartReceiver().onReceive(context, intent);
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/service/CustomRefreshActionReceiver.java << 'EOF'
package com.yxlanyu.refreshrate.service;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
public class CustomRefreshActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !"STOP_CUSTOM_REFRESH".equals(intent.getAction())) return;
        SharedPreferences prefs = context.getSharedPreferences("s", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("custom_app_refresh", false).apply();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(1002);
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/util/AutoOverclockManager.java << 'EOF'
package com.yxlanyu.refreshrate.util;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.service.OverclockService;
import java.util.*;
public class AutoOverclockManager {
    private static final String TAG = "AutoOverclock";
    private static final long STEP_DELAY_MS = 800;
    private static final long POLL_DELAY_MS = 2000;
    private static Thread  daemonThread;
    private static volatile boolean running  = false;
    private static volatile String lastLog = "";
    private static volatile int    targetW   = 0;
    private static volatile int    targetH   = 0;
    private static volatile int    targetHz  = 0;
    private static volatile String authMode  = "";
    private static Context appContext;
    private static Context getLocalizedContext() {
        if (appContext == null) return null;
        try {
            SharedPreferences prefs = appContext.getSharedPreferences("s", android.content.Context.MODE_PRIVATE);
            String lang = prefs.getString("language", "zh");
            java.util.Locale locale = "zh-rTW".equals(lang) ? new java.util.Locale("zh", "TW") : new java.util.Locale(lang);
            android.content.res.Configuration config = new android.content.res.Configuration(appContext.getResources().getConfiguration());
            config.setLocale(locale);
            return appContext.createConfigurationContext(config);
        } catch (Exception e) { return appContext;
        }
    }
    public static String getLastLog()  { return lastLog;
    }
    public static boolean isRunning()  { return running;
    }
    public static void startService(Context ctx, String mode,
                                    int tw, int th, int hz) {
        if (running) return;
        Intent intent = new Intent(ctx, OverclockService.class);
        intent.putExtra("auth_mode", mode);
        intent.putExtra("targetW", tw);
        intent.putExtra("targetH", th);
        intent.putExtra("targetHz", hz);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent);
        else
            ctx.startService(intent);
    }
    public static void stopService(Context ctx) {
        stop();
        ctx.stopService(new Intent(ctx, OverclockService.class));
    }
    public static void updateTarget(int tw, int th, int hz) {
        targetW  = tw;
        targetH  = th;
        targetHz = hz;
        if (appContext != null) lastLog = getLocalizedContext().getString(R.string.guard_target_updated, tw + "×" + th, hz);
        Log.d(TAG, lastLog);
    }
    public static void start(Context ctx, String mode,
                             int tw, int th, int hz) {
        if (running) return;appContext = ctx.getApplicationContext();
        authMode = mode;
        targetW  = tw;
        targetH  = th;
        targetHz = hz;
        running  = true;
        lastLog  = getLocalizedContext().getString(R.string.guard_start_format, tw + "×" + th, hz);
        daemonThread = new Thread(() -> {
            while (running) {
                try {
                    int curTargetW  = targetW;
                    int curTargetH  = targetH;
                    int curTargetHz = targetHz;
                    List<DisplayMode> all = getSupportedModes(ctx);
                    List<DisplayMode> filtered = new ArrayList<>();
                    for (DisplayMode m : all) {
                        if (m.getWidth() == curTargetW && m.getHeight() == curTargetH)
                            filtered.add(m);
                    }
                    if (filtered.isEmpty()) {
                        lastLog = getLocalizedContext().getString(R.string.guard_no_res_format, curTargetW + "×" + curTargetH);
                        Thread.sleep(POLL_DELAY_MS);
                        continue;
                    }
                    filtered.sort((a, b) -> Float.compare(a.getRefreshRate(), b.getRefreshRate()));
                    DisplayMode finalTarget = filtered.get(filtered.size() - 1);
                    for (DisplayMode m : filtered) {
                        if (m.getRateInt() == curTargetHz) { finalTarget = m; break; }
                    }
                    float curRate = getCurrentRate(ctx);
                    int   curInt  = Math.round(curRate);
                    String curRes = getCurrentResolution(ctx);
                    String tgtRes = curTargetW + "×" + curTargetH;
                    boolean resOk  = tgtRes.equals(curRes);
                    boolean rateOk = (curInt == finalTarget.getRateInt());
                    if (resOk && rateOk) {
                        lastLog = getLocalizedContext().getString(R.string.guard_watching_format, finalTarget.getRateInt());
                        OverclockService.updateNotification(ctx);
                        Thread.sleep(POLL_DELAY_MS);
                        continue;
                    }
                    int curIdx = 0;
                    if (resOk) {
                        for (int i = filtered.size() - 1; i >= 0; i--) {
                            if (filtered.get(i).getRateInt() <= curInt) {
                             curIdx = i; break;
                            }
                        }
                    }
                    DisplayMode next;
                    if (curInt < finalTarget.getRateInt()) {
                        int nextIdx = Math.min(curIdx + 1, filtered.size() - 1);
                        while (nextIdx < filtered.size() - 1 && filtered.get(nextIdx).getRateInt() > finalTarget.getRateInt())
                            nextIdx--;
                        next = filtered.get(nextIdx);
                        String prefix = !resOk ? getLocalizedContext().getString(R.string.guard_res_fix) : "";
                        lastLog = getLocalizedContext().getString(R.string.guard_hz_up, prefix, curInt, next.getRateInt());
                    } else {
                        int nextIdx = Math.max(curIdx - 1, 0);
                        while (nextIdx > 0 && filtered.get(nextIdx).getRateInt() < finalTarget.getRateInt())
                            nextIdx++;
                        next = filtered.get(nextIdx);
                        lastLog = getLocalizedContext().getString(R.string.guard_hz_down, curInt, next.getRateInt());
                    }
                    Log.d(TAG, lastLog);
                    if ("root".equals(authMode)) {
                        RootUtils.setDisplayMode(next.getWidth(), next.getHeight(), next.getRateInt(), next.getSfIndex());
                    } else if ("shizuku".equals(authMode)) {
                        ShizukuUtils.setDisplayMode(next.getWidth(), next.getHeight(), next.getRateInt(), next.getSfIndex());
                    }
                    OverclockService.updateNotification(ctx);
                    Thread.sleep(STEP_DELAY_MS);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "daemon: " + e.getMessage());
                    try { Thread.sleep(POLL_DELAY_MS); } catch (InterruptedException ex) { break; }
                }
            }
            running = false;
            if (appContext != null) lastLog = getLocalizedContext().getString(R.string.guard_stopped);
        });
        daemonThread.setDaemon(true);
        daemonThread.start();
    }
    public static void stop() {
        running = false;
        if (daemonThread != null) { daemonThread.interrupt(); daemonThread = null; }
    }
    public static float getCurrentRate(Context ctx) {
        try {
            DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
            return dm.getDisplay(Display.DEFAULT_DISPLAY).getRefreshRate();
        } catch (Exception e) { return 0f; }
    }
    public static String getCurrentResolution(Context ctx) {
        try {
            DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
            Display.Mode m = dm.getDisplay(Display.DEFAULT_DISPLAY).getMode();
            return m.getPhysicalWidth() + "×" + m.getPhysicalHeight();
        } catch (Exception e) { return "--";
        }
    }
    public static List<DisplayMode> getSupportedModes(Context ctx) {
        List<DisplayMode> list = new ArrayList<>();
        try {
            DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
            Display.Mode[] modes = dm.getDisplay(Display.DEFAULT_DISPLAY).getSupportedModes();
            int sfIdx = 0;
            for (Display.Mode m : modes) {
                DisplayMode dmode = new DisplayMode(m.getPhysicalWidth(), m.getPhysicalHeight(),
                        m.getRefreshRate(), m.getModeId());
                dmode.setSfIndex(sfIdx++);
                list.add(dmode);
            }
        } catch (Exception ignored) {}
        return list;
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/adapter/RateAdapter.java << 'EOF'
package com.yxlanyu.refreshrate.adapter;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.DisplayMode;
import java.util.List;
public class RateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0, TYPE_RATE = 1;
    public interface OnRateClick { void onClick(DisplayMode m); }
    private List<Object> items;
    private Context ctx; private OnRateClick listener;
    private DisplayMode current;
    private int lastCurrentPos = -1;
    public RateAdapter(Context c, List<Object> i, OnRateClick l) { ctx=c; items=i; listener=l;
    }
    public void update(List<Object> i) { items=i;
    notifyDataSetChanged(); }
    private static final Object PAYLOAD_SEL = new Object();
    public void setCurrent(DisplayMode m) {
        int oldPos = lastCurrentPos;
        int newPos = findPosition(m);
        current = m;
        lastCurrentPos = newPos;
        if (oldPos != newPos) {
            if (oldPos >= 0) notifyItemChanged(oldPos, PAYLOAD_SEL);
            if (newPos >= 0) notifyItemChanged(newPos, PAYLOAD_SEL);
        }
    }
    public void setCurrentByWHR(int w, int h, int hz) {
        for (Object o : items) {
            if (o instanceof DisplayMode) {
                DisplayMode dm = (DisplayMode) o;
                if (dm.getWidth() == w && dm.getHeight() == h && dm.getRateInt() == hz) { setCurrent(dm); return; }
            }
        }
        for (Object o : items) {
            if (o instanceof DisplayMode) {
                DisplayMode dm = (DisplayMode) o;
                if (dm.getRateInt() == hz) { setCurrent(dm); return; }
            }
        }
    }
    private int findPosition(DisplayMode target) {
        for (int i = 0; i < items.size(); i++) {
            Object obj = items.get(i);
            if (obj instanceof DisplayMode) {
                DisplayMode dm = (DisplayMode) obj;
                if (dm.getModeId() == target.getModeId() && dm.getRateInt() == target.getRateInt()) {
                    return i;
                }
            }
        }
        return -1;
    }
    @Override public int getItemViewType(int p) { return items.get(p) instanceof String ? TYPE_HEADER : TYPE_RATE; }
    @Override public int getItemCount() { return items==null ? 0 : items.size(); }
    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p, int t) {
        LayoutInflater inf = LayoutInflater.from(ctx);
        if (t == TYPE_HEADER) return new HVH(inf.inflate(R.layout.item_res_header, p, false));
        return new RVH(inf.inflate(R.layout.item_rate_card, p, false));
    }
    @Override public void onBindViewHolder(RecyclerView.ViewHolder h, int pos, java.util.List<Object> payloads) {
        if (!payloads.isEmpty() && getItemViewType(pos) == TYPE_RATE) {
            DisplayMode m = (DisplayMode) items.get(pos);
            RVH r = (RVH) h;
            boolean act = current != null && current.getModeId() == m.getModeId() && current.getRateInt() == m.getRateInt();
            applySelection(r, colorFor(m.getRateInt()), act, true);
            return;
        }
        super.onBindViewHolder(h, pos, payloads);
    }
    private void applySelection(RVH r, int color, boolean act, boolean animate) {
        if (act) {
            r.card.setCardBackgroundColor(darkenColor(color, 0.75f));
            r.card.setStrokeColor(0xCCFFFFFF);
            r.card.setStrokeWidth(dpToPx(2));
        } else {
            r.card.setCardBackgroundColor(color);
            r.card.setStrokeColor(0x50FFFFFF);
            r.card.setStrokeWidth(dpToPx(1));
        }
        if (animate && act) {
            r.card.animate().cancel();
            r.card.setScaleX(0.94f); r.card.setScaleY(0.94f);
            r.card.animate().scaleX(1f).scaleY(1f).setDuration(220).setInterpolator(new android.view.animation.OvershootInterpolator(2.5f)).start();
        }
    }
    @Override public void onBindViewHolder(RecyclerView.ViewHolder h, int pos) {
        h.itemView.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.item_animation));
        if (getItemViewType(pos) == TYPE_HEADER) {
            ((HVH) h).title.setText((String) items.get(pos));
        } else {
            DisplayMode m = (DisplayMode) items.get(pos);
            RVH r = (RVH) h;
            r.name.setText(m.getRateName(ctx));
            r.hz.setText(m.getRateInt() + " Hz");
            r.desc.setText(m.getRateDesc(ctx));
            int color = colorFor(m.getRateInt());
            boolean act = current != null && current.getModeId() == m.getModeId() && current.getRateInt() == m.getRateInt();
            applySelection(r, color, act, false);
            r.card.setOnClickListener(v -> { if (listener != null) listener.onClick(m); });
            r.debug.setVisibility(View.GONE);
        }
    }
    private int colorFor(int hz) {
        if (hz >= 185) return Color.argb(170, 240, 213, 45);
        if (hz >= 165) return Color.argb(170, 230, 100, 150);
        if (hz >= 144) return Color.argb(170, 242, 140, 56);
        if (hz >= 120) return Color.argb(170, 50, 124, 216);
        if (hz >= 90)  return Color.argb(170, 40, 169, 160);
        if (hz >= 60)  return Color.argb(170, 59, 156, 89);
        return Color.argb(170, 67, 160, 71);
    }
    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
    private int dpToPx(int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }
    static class HVH extends RecyclerView.ViewHolder {
        TextView title;
        HVH(View v) { super(v); title = v.findViewById(R.id.tv_header_title); }
    }
    static class RVH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView name, hz, desc, debug;
        RVH(View v) {
            super(v);
            card = v.findViewById(R.id.card_rate);
            name = v.findViewById(R.id.tv_rate_name);
            hz = v.findViewById(R.id.tv_rate_hz);
            desc = v.findViewById(R.id.tv_rate_desc);
            debug = v.findViewById(R.id.tv_rate_debug);
        }
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/adb/AdbConnectionManager.java << 'EOF'
package com.yxlanyu.refreshrate.adb;
import android.content.Context;
import android.os.Build;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import io.github.muntashirakon.adb.AbsAdbConnectionManager;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
public class AdbConnectionManager extends AbsAdbConnectionManager {
    private static AbsAdbConnectionManager INSTANCE;
    public static AbsAdbConnectionManager getInstance(Context context) throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new AdbConnectionManager(context.getApplicationContext());
        }
        return INSTANCE;
    }
    public static void reset() {
        INSTANCE = null;
    }
    public static AbsAdbConnectionManager newInstance(Context context) throws Exception {
        return new AdbConnectionManager(context.getApplicationContext());
    }
    private PrivateKey mPrivateKey;
    private Certificate mCertificate;
    private final File keyFile;
    private final File certFile;
    private AdbConnectionManager(Context context) throws Exception {
        setApi(Build.VERSION.SDK_INT);
        keyFile = new File(context.getFilesDir(), "adbkey");
        certFile = new File(context.getFilesDir(), "adbcert.pem");
        mPrivateKey = readPrivateKey();
        mCertificate = readCertificate();
        if (mPrivateKey == null || mCertificate == null) {
            generate();
        }
    }
    private void generate() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();
        mPrivateKey = keyPair.getPrivate();
        X500Name x500 = new X500Name("CN=RefreshRate");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - 86400000L);
        Date notAfter = new Date(System.currentTimeMillis() + 10L * 365 * 24 * 60 * 60 * 1000);
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                x500, serial, notBefore, notAfter, x500, keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(mPrivateKey);
        mCertificate = new JcaX509CertificateConverter().getCertificate(builder.build(signer));
        FileOutputStream kos = new FileOutputStream(keyFile);
        kos.write(mPrivateKey.getEncoded());
        kos.close();
        FileOutputStream cos = new FileOutputStream(certFile);
        cos.write(mCertificate.getEncoded());
        cos.close();
    }
    private PrivateKey readPrivateKey() {
        try {
            if (!keyFile.exists()) return null;
            byte[] bytes = readAll(keyFile);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (Exception e) {
            return null;
        }
    }
    private Certificate readCertificate() {
        try {
            if (!certFile.exists()) return null;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            FileInputStream fis = new FileInputStream(certFile);
            Certificate c = cf.generateCertificate(fis);
            fis.close();
            return c;
        } catch (Exception e) {
            return null;
        }
    }
    private static byte[] readAll(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = fis.read(buf)) > 0) bos.write(buf, 0, n);
        fis.close();
        return bos.toByteArray();
    }
    @Override
    protected PrivateKey getPrivateKey() {
        return mPrivateKey;
    }
    @Override
    protected Certificate getCertificate() {
        return mCertificate;
    }
    @Override
    protected String getDeviceName() {
        return "RefreshRate";
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/service/RemoteKeepAliveService.java << 'EOF'
package com.yxlanyu.refreshrate.service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
public class RemoteKeepAliveService extends Service {
    private static final String CHANNEL_ID = "remote_keepalive_channel";
    private static final int NOTIFICATION_ID = 1007;
    public static void start(Context c) {
        try {
            Intent i = new Intent(c, RemoteKeepAliveService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) c.startForegroundService(i);
            else c.startService(i);
        } catch (Throwable ignored) {}
    }
    public static void stop(Context c) {
        try { c.stopService(new Intent(c, RemoteKeepAliveService.class)); } catch (Throwable ignored) {}
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "远程连接保持", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Notification.Builder b = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
            b.setContentTitle("远程刷新率").setContentText("正在保持设备连接").setSmallIcon(android.R.drawable.stat_sys_data_bluetooth).setOngoing(true);
            startForeground(NOTIFICATION_ID, b.build());
        } catch (Throwable ignored) {}
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
EOF
cat > app/src/main/java/com/yxlanyu/refreshrate/fragment/RemoteRefreshFragment.java << 'EOF'
package com.yxlanyu.refreshrate.fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.adapter.RateAdapter;
import com.yxlanyu.refreshrate.adb.AdbConnectionManager;
import com.yxlanyu.refreshrate.model.DisplayMode;
import io.github.muntashirakon.adb.AbsAdbConnectionManager;
import io.github.muntashirakon.adb.AdbStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class RemoteRefreshFragment extends Fragment {
    private static final int GREEN = 0xFF2ECC71, AMBER = 0xFFFFB300, RED = 0xFFE57373;
    private SharedPreferences prefs;
    private boolean wirelessMode = true;
    private boolean connected = false;
    private AbsAdbConnectionManager manager;
    private final java.util.List<DisplayMode> remoteModes = new java.util.ArrayList<>();
    private EditText etIp, etPort, etPair, etPairPort;
    private TextView tvStatus, tvModesEmpty, tvHistoryTitle;
    private LinearLayout llWirelessInputs, llUsbHint, llHistory;
    private RecyclerView rvModes;
    private TextView btnWireless;
    private TextView btnUsb;
    private TextView tvDevice;
    private androidx.appcompat.widget.SwitchCompat swBlurIp;
    private LinearLayout llDevices;
    private static final int MAX_DEV = 3;
    private static final java.util.List<RemoteDev> HUB = new java.util.ArrayList<>();
    private static int activeIdx = -1;
    static class RemoteDev {
        String ip, port, model = "", dump = "", devInfo = "";
        AbsAdbConnectionManager mgr;
        RemoteDev(String ip, String port, AbsAdbConnectionManager mgr) { this.ip = ip; this.port = port; this.mgr = mgr; }
    }
    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle s) {
        View v = inf.inflate(R.layout.fragment_remote, container, false);
        prefs = requireActivity().getSharedPreferences("s", Context.MODE_PRIVATE);
        btnWireless = v.findViewById(R.id.btn_mode_wireless);
        btnUsb = v.findViewById(R.id.btn_mode_usb);
        btnWireless.setOnClickListener(x -> setMode(true));
        btnUsb.setOnClickListener(x -> setMode(false));
        llWirelessInputs = v.findViewById(R.id.ll_wireless_inputs);
        llUsbHint = v.findViewById(R.id.ll_usb_hint);
        llHistory = v.findViewById(R.id.ll_remote_history);
        tvHistoryTitle = v.findViewById(R.id.tv_history_title);
        etIp = v.findViewById(R.id.et_remote_ip);
        etPort = v.findViewById(R.id.et_remote_port);
        etPair = v.findViewById(R.id.et_remote_pair);
        etPairPort = v.findViewById(R.id.et_remote_pair_port);
        tvStatus = v.findViewById(R.id.tv_remote_status);
        tvModesEmpty = v.findViewById(R.id.tv_remote_modes_empty);
        rvModes = v.findViewById(R.id.recycler_remote_modes);
        rvModes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvModes.setNestedScrollingEnabled(false);
        tvDevice = v.findViewById(R.id.tv_remote_device);
        swBlurIp = v.findViewById(R.id.switch_blur_ip);
        swBlurIp.setChecked(prefs.getBoolean("remote_blur_ip", false));
        swBlurIp.setOnCheckedChangeListener((b, on) -> {
            prefs.edit().putBoolean("remote_blur_ip", on).apply();
            applyIpMask();
        });
        etIp.setOnFocusChangeListener((vv, has) -> applyIpMask());
        applyIpMask();
        applyCardAlpha(v);
        llDevices = v.findViewById(R.id.ll_remote_devices);
        renderChips();
        if (activeIdx >= 0 && activeIdx < HUB.size()) {
            RemoteDev d = HUB.get(activeIdx);
            manager = d.mgr; connected = true;
            etIp.setText(d.ip); etPort.setText(d.port);
            applyIpMask();
            populateModes(d.dump); setDeviceInfo(d.devInfo);
            setStatus(connectedText(d.ip), GREEN);
        }
        setMode(true);
        refreshHistory();
        v.findViewById(R.id.btn_remote_pair).setOnClickListener(x -> doPair());
        v.findViewById(R.id.btn_remote_connect).setOnClickListener(x -> doConnect());
        return v;
    }
    @Override
    public void onResume() {
        super.onResume();
        applyIpMask();
        applyRemoteAlpha();
    }
    private void applyRemoteAlpha() {
        if (prefs == null) return;
        float cardA = 1f - (prefs.getInt("remote_card_alpha", 0) / 100f);
        float textA = 1f - (prefs.getInt("remote_text_alpha", 0) / 100f);
        View root = getView();
        if (root instanceof android.view.ViewGroup) applyAlphaRec((android.view.ViewGroup) root, cardA, textA);
    }
    private void applyAlphaRec(android.view.ViewGroup vg, float cardA, float textA) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            android.view.View c = vg.getChildAt(i);
            if (c instanceof androidx.cardview.widget.CardView) {
                c.setAlpha(cardA);
            } else if (c instanceof android.widget.TextView) {
                c.setAlpha(textA);
            }
            if (c instanceof android.view.ViewGroup) applyAlphaRec((android.view.ViewGroup) c, cardA, textA);
        }
    }
    private void applyIpMask() {
        if (etIp == null) return;
        boolean mask = prefs.getBoolean("remote_blur_ip", false) && !etIp.hasFocus();
        etIp.setTransformationMethod(mask ? android.text.method.PasswordTransformationMethod.getInstance() : null);
    }
    private void setDeviceInfo(String raw) {
        if (tvDevice == null) return;
        if (raw == null) { tvDevice.setVisibility(View.GONE); return; }
        String[] L = raw.split("\n");
        String market = L.length > 0 ? L[0].trim() : "";
        String oplus = L.length > 1 ? L[1].trim() : "";
        String model = L.length > 2 ? L[2].trim() : "";
        String ver = L.length > 3 ? L[3].trim() : "";
        String name = !market.isEmpty() ? market : (!oplus.isEmpty() ? oplus : model);
        if (name.isEmpty()) { tvDevice.setVisibility(View.GONE); return; }
        String txt = name;
        if (!ver.isEmpty()) txt += "  ·  Android " + ver;
        tvDevice.setText(txt);
        tvDevice.setVisibility(View.VISIBLE);
    }
    private int findDev(String ip, String port) {
        for (int i = 0; i < HUB.size(); i++)
            if (HUB.get(i).ip.equals(ip) && HUB.get(i).port.equals(port)) return i;
        return -1;
    }
    private static String parseName(String raw) {
        if (raw == null) return "";
        String[] L = raw.split("\n");
        String market = L.length > 0 ? L[0].trim() : "";
        String oplus = L.length > 1 ? L[1].trim() : "";
        String model = L.length > 2 ? L[2].trim() : "";
        return !market.isEmpty() ? market : (!oplus.isEmpty() ? oplus : model);
    }
    private void selectDevice(int idx) {
        if (idx < 0 || idx >= HUB.size()) return;
        activeIdx = idx;
        RemoteDev d = HUB.get(idx);
        manager = d.mgr; connected = true;
        etIp.setText(d.ip); etPort.setText(d.port);
        applyIpMask();
        populateModes(d.dump);
        setDeviceInfo(d.devInfo);
        setStatus(connectedText(d.ip), GREEN);
        renderChips();
    }
    private void disconnectDevice(int idx) {
        if (idx < 0 || idx >= HUB.size()) return;
        try { HUB.get(idx).mgr.getClass().getMethod("close").invoke(HUB.get(idx).mgr); } catch (Throwable ig) {}
        HUB.remove(idx);
        if (HUB.isEmpty()) {
            activeIdx = -1; manager = null; connected = false;
            showEmpty(R.string.remote_status_none);
            if (tvDevice != null) tvDevice.setVisibility(View.GONE);
            setStatus(getString(R.string.remote_status_idle), AMBER);
            try { com.yxlanyu.refreshrate.service.RemoteKeepAliveService.stop(requireContext().getApplicationContext()); } catch (Throwable ig) {}
            renderChips();
        } else {
            selectDevice(0);
        }
    }
    private void renderChips() {
        if (llDevices == null) return;
        llDevices.removeAllViews();
        if (HUB.isEmpty()) { llDevices.setVisibility(View.GONE); return; }
        llDevices.setVisibility(View.VISIBLE);
        for (int i = 0; i < HUB.size(); i++) {
            final int idx = i;
            RemoteDev d = HUB.get(i);
            boolean active = (i == activeIdx);
            LinearLayout chip = new LinearLayout(getContext());
            chip.setOrientation(LinearLayout.HORIZONTAL);
            chip.setGravity(android.view.Gravity.CENTER_VERTICAL);
            chip.setBackgroundColor(active ? 0xFF1976D2 : 0x1A000000);
            chip.setPadding(dp(12), dp(7), dp(8), dp(7));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(8), 0);
            chip.setLayoutParams(lp);
            TextView label = new TextView(getContext());
            label.setText(d.model.isEmpty() ? d.ip : d.model);
            label.setTextSize(13);
            label.setTextColor(active ? 0xFFFFFFFF : 0xFF555555);
            label.setOnClickListener(x -> selectDevice(idx));
            chip.addView(label);
            TextView del = new TextView(getContext());
            del.setText("✕");
            del.setTextSize(13);
            del.setTextColor(active ? 0xFFFFFFFF : 0xFF888888);
            del.setPadding(dp(9), 0, dp(2), 0);
            del.setOnClickListener(x -> disconnectDevice(idx));
            chip.addView(del);
            llDevices.addView(chip);
        }
    }
    private void setMode(boolean wireless) {
        wirelessMode = wireless;
        llWirelessInputs.setVisibility(wireless ? View.VISIBLE : View.GONE);
        llUsbHint.setVisibility(wireless ? View.GONE : View.VISIBLE);
        styleTab(btnWireless, wireless);
        styleTab(btnUsb, !wireless);
        View vv = getView();
        if (vv != null) {
            View modesCard = vv.findViewById(R.id.card_remote_modes);
            if (modesCard != null) modesCard.setVisibility(wireless ? View.VISIBLE : View.GONE);
            if (tvStatus != null) tvStatus.setVisibility(wireless ? View.VISIBLE : View.GONE);
        }
    }
    private void styleTab(TextView tab, boolean selected) {
        if (tab == null) return;
        tab.setBackgroundResource(selected ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tab.setTextColor(selected ? 0xFFFFFFFF : 0xFF546E7A);
    }
    private String connectedText(String ip) {
        if (prefs.getBoolean("remote_blur_ip", false))
            return getString(R.string.remote_connected, getString(R.string.remote_wireless_device));
        return getString(R.string.remote_connected, ip);
    }
    private void applyCardAlpha(View v) {
        int pref = prefs.getInt("setting_card_alpha", 0);
        int a = 255 - (int) (pref / 100f * 255);
        if (a < 0) a = 0;
        if (a > 255) a = 255;
        int color = (a << 24) | 0x00FFFFFF;
        int[] ids = { R.id.card_remote_conn, R.id.card_remote_modes };
        for (int id : ids) {
            View card = v.findViewById(id);
            if (card instanceof CardView) ((CardView) card).setCardBackgroundColor(color);
        }
    }
    private void doPair() {
        if (!wirelessMode) return;
        final String ip = etIp.getText().toString().trim();
        final String port = etPairPort.getText().toString().trim();
        final String code = etPair.getText().toString().trim();
        if (ip.isEmpty() || port.isEmpty() || code.isEmpty()) {
            setStatus(getString(R.string.remote_need_pair_input), RED);
            return;
        }
        setStatus(getString(R.string.remote_pairing), AMBER);
        new Thread(() -> {
            boolean ok = false;
            String err = null;
            try {
                AbsAdbConnectionManager m = AdbConnectionManager.newInstance(requireContext());
                ok = m.pair(ip, Integer.parseInt(port), code);
            } catch (Throwable t) {
                err = String.valueOf(t.getMessage());
            }
            final boolean fok = ok;
            final String ferr = err;
            post(() -> {
                if (fok) setStatus(getString(R.string.remote_pair_ok), GREEN);
                else setStatus(getString(R.string.remote_pair_fail, ferr), RED);
            });
        }).start();
    }
    private void doConnect() {
        if (!wirelessMode) return;
        final String ip = etIp.getText().toString().trim();
        final String port = etPort.getText().toString().trim();
        if (ip.isEmpty() || port.isEmpty()) {
            setStatus(getString(R.string.remote_status_need_input), RED);
            return;
        }
        setStatus(getString(R.string.remote_conn_wait), AMBER);
        new Thread(() -> {
            boolean ok = false;
            String err = null;
            String dump = null;
            String devInfo = null;
            AbsAdbConnectionManager m = null;
            try {
                m = AdbConnectionManager.newInstance(requireContext());
                ok = m.connect(ip, Integer.parseInt(port));
                if (ok) {
                    dump = exec(m, "dumpsys display");
                    devInfo = exec(m, "getprop ro.product.marketname; getprop ro.vendor.oplus.market.name; getprop ro.product.model; getprop ro.build.version.release");
                }
            } catch (Throwable t) {
                err = String.valueOf(t.getMessage());
            }
            final boolean fok = ok;
            final String ferr = err;
            final String fdump = dump;
            final String fdevInfo = devInfo;
            final AbsAdbConnectionManager fm = m;
            post(() -> {
                if (fok) {
                    int slot = findDev(ip, port);
                    if (slot < 0 && HUB.size() >= MAX_DEV) {
                        setStatus(getString(R.string.remote_max_devices), AMBER);
                        return;
                    }
                    RemoteDev d;
                    if (slot >= 0) {
                        d = HUB.get(slot);
                        try { d.mgr.getClass().getMethod("close").invoke(d.mgr); } catch (Throwable ig) {}
                        d.mgr = fm; activeIdx = slot;
                    } else {
                        d = new RemoteDev(ip, port, fm); HUB.add(d); activeIdx = HUB.size() - 1;
                    }
                    d.dump = fdump == null ? "" : fdump;
                    d.devInfo = fdevInfo == null ? "" : fdevInfo;
                    d.model = parseName(d.devInfo);
                    manager = fm; connected = true;
                    saveHistory(ip, port);
                    setStatus(connectedText(ip), GREEN);
                    populateModes(d.dump);
                    setDeviceInfo(d.devInfo);
                    renderChips();
                    refreshHistory();
                    com.yxlanyu.refreshrate.service.RemoteKeepAliveService.start(requireContext().getApplicationContext());
                } else {
                    setStatus(getString(R.string.remote_conn_fail, ferr), RED);
                }
            });
        }).start();
    }
    private static String exec(AbsAdbConnectionManager m, String cmd) throws Exception {
        AdbStream stream = m.openStream("shell:" + cmd);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            InputStream is = stream.openInputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
        } catch (Exception readEnd) {
        } finally {
            try { stream.close(); } catch (Exception ignored) {}
        }
        return new String(bos.toByteArray());
    }
    private void populateModes(String dump) {
        if (dump == null) {
            showEmpty(R.string.remote_modes_parse_fail);
            return;
        }
        Pattern p = Pattern.compile("id=(\\d+),\\s*width=(\\d+),\\s*height=(\\d+),\\s*fps=([0-9.]+)");
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        LinkedHashMap<String, List<DisplayMode>> groups = new LinkedHashMap<>();
        int sfIdx = 0;
        boolean usedRecords = false;
        for (String line : dump.split("\n")) {
            if (!line.contains("DisplayModeRecord")) continue;
            Matcher m = p.matcher(line);
            if (m.find()) {
                usedRecords = true;
                addMode(groups, seen, m, sfIdx);
                sfIdx++;
            }
        }
        if (!usedRecords) {
            Matcher m = p.matcher(dump);
            while (m.find()) addMode(groups, seen, m, -1);
        }
        if (groups.isEmpty()) {
            showEmpty(R.string.remote_modes_parse_fail);
            return;
        }
        List<Object> items = new ArrayList<>();
        for (Map.Entry<String, List<DisplayMode>> en : groups.entrySet()) {
            List<DisplayMode> g = en.getValue();
            Collections.sort(g, (a, b) -> b.getRateInt() - a.getRateInt());
            items.add(en.getKey());
            items.addAll(g);
        }
        remoteModes.clear();
        for (List<DisplayMode> g : groups.values()) remoteModes.addAll(g);
        tvModesEmpty.setVisibility(View.GONE);
        rvModes.setVisibility(View.VISIBLE);
        RateAdapter ad = new RateAdapter(getContext(), items,
                m -> applyRemoteMode(m.getWidth(), m.getHeight(), m.getRateInt(), m.getSfIndex()));
        rvModes.setAdapter(ad);
    }
    private static void addMode(LinkedHashMap<String, List<DisplayMode>> groups, LinkedHashSet<String> seen, Matcher m, int sfIndex) {
        int id = Integer.parseInt(m.group(1));
        int w = Integer.parseInt(m.group(2));
        int h = Integer.parseInt(m.group(3));
        float fps = Float.parseFloat(m.group(4));
        String key = w + "x" + h + "@" + Math.round(fps);
        if (!seen.add(key)) return;
        DisplayMode dm = new DisplayMode(w, h, fps, id);
        if (sfIndex >= 0) dm.setSfIndex(sfIndex);
        String res = w + " × " + h;
        List<DisplayMode> g = groups.get(res);
        if (g == null) {
            g = new ArrayList<>();
            groups.put(res, g);
        }
        g.add(dm);
    }
    private void showEmpty(int res) {
        if (rvModes != null) rvModes.setVisibility(View.GONE);
        tvModesEmpty.setVisibility(View.VISIBLE);
        tvModesEmpty.setText(res);
    }
    private void applyRemoteMode(int w, int h, int rr, int sfIndex) {
        if (!connected || manager == null) return;
        setStatus(getString(R.string.remote_switching, rr), AMBER);
        new Thread(() -> {
            int active = -1;
            String err = null;
            try {
                String setCmd = "cmd display set-user-preferred-display-mode " + w + " " + h + " " + rr + " 2>/dev/null; "
                        + "settings put system peak_refresh_rate " + rr + ".0; "
                        + "settings put system min_refresh_rate " + rr + ".0; "
                        + "settings put system user_refresh_rate " + rr + "; "
                        + "settings put secure miui_refresh_rate " + rr + "; "
                        + "settings put system thermal_limit_refresh_rate " + rr + " 2>/dev/null";
                if (sfIndex >= 0) setCmd += "; service call SurfaceFlinger 1035 i32 " + sfIndex;
                exec(manager, setCmd);
                Thread.sleep(600);
                active = parseActiveFps(exec(manager, "dumpsys display"));
            } catch (Throwable t) {
                err = String.valueOf(t.getMessage());
            }
            final int fa = active;
            final String ferr = err;
            post(() -> {
                if (ferr != null) setStatus(getString(R.string.remote_switch_fail, ferr), RED);
                else if (fa <= 0) setStatus(getString(R.string.remote_switch_sent), AMBER);
                else if (fa == rr) setStatus(getString(R.string.remote_switch_ok, rr), GREEN);
                else setStatus(getString(R.string.remote_switch_actual, fa), AMBER);
            });
        }).start();
    }
    private static int parseActiveFps(String dump) {
        if (dump == null) return -1;
        try {
            Matcher a = Pattern.compile("mActiveModeId=(\\d+)").matcher(dump);
            if (a.find()) {
                int id = Integer.parseInt(a.group(1));
                Matcher f = Pattern.compile("id=" + id + ", width=\\d+, height=\\d+, fps=([0-9.]+)").matcher(dump);
                if (f.find()) return Math.round(Float.parseFloat(f.group(1)));
            }
        } catch (Exception e) {}
        try {
            Matcher r = Pattern.compile("refreshRate=?\\s?([0-9]+(?:\\.[0-9]+)?)").matcher(dump);
            if (r.find()) return Math.round(Float.parseFloat(r.group(1)));
        } catch (Exception e) {}
        return -1;
    }
    private void saveHistory(String ip, String port) {
        String entry = ip + "," + port;
        String raw = prefs.getString("remote_history", "");
        ArrayList<String> list = new ArrayList<>();
        list.add(entry);
        if (!raw.isEmpty()) {
            for (String e : raw.split(";")) {
                if (!e.isEmpty() && !e.equals(entry) && list.size() < 6) list.add(e);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(list.get(i));
        }
        prefs.edit().putString("remote_history", sb.toString()).apply();
    }
    private void refreshHistory() {
        String raw = prefs.getString("remote_history", "");
        llHistory.removeAllViews();
        if (raw.isEmpty()) {
            tvHistoryTitle.setVisibility(View.GONE);
            llHistory.setVisibility(View.GONE);
            return;
        }
        tvHistoryTitle.setVisibility(View.VISIBLE);
        llHistory.setVisibility(View.VISIBLE);
        for (String e : raw.split(";")) {
            if (e.isEmpty()) continue;
            String[] parts = e.split(",");
            if (parts.length < 2) continue;
            final String ip = parts[0];
            final String port = parts[1];
            final String entry = e;
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(0x0D1976D2);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.topMargin = dp(6);
            row.setLayoutParams(rlp);
            TextView txt = new TextView(getContext());
            txt.setText(ip + " : " + port);
            txt.setTextSize(14);
            txt.setTextColor(0xFF1976D2);
            txt.setPadding(dp(12), dp(11), dp(8), dp(11));
            txt.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            txt.setOnClickListener(x -> {
                etIp.setText(ip);
                etPort.setText(port);
                applyIpMask();
            });
            TextView del = new TextView(getContext());
            del.setText("✕");
            del.setTextSize(15);
            del.setTextColor(0xFFE57373);
            del.setPadding(dp(14), dp(11), dp(16), dp(11));
            del.setOnClickListener(x -> {
                deleteHistory(entry);
                refreshHistory();
            });
            row.addView(txt);
            row.addView(del);
            llHistory.addView(row);
        }
    }
    private void deleteHistory(String entry) {
        String raw = prefs.getString("remote_history", "");
        ArrayList<String> list = new ArrayList<>();
        for (String e : raw.split(";")) {
            if (!e.isEmpty() && !e.equals(entry)) list.add(e);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(list.get(i));
        }
        prefs.edit().putString("remote_history", sb.toString()).apply();
    }
    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density);
    }
    private void post(Runnable r) {
        if (isAdded() && getActivity() != null) getActivity().runOnUiThread(r);
    }
    private void setStatus(String s, int color) {
        if (tvStatus != null) {
            tvStatus.setText(s);
            tvStatus.setTextColor(color);
        }
    }
}
EOF

cat > app/src/main/res/drawable/bg_tab_selected.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#1976D2"/>
    <corners android:radius="12dp"/>
</shape>
EOF
cat > app/src/main/res/drawable/bg_tab_unselected.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#E8EEF3"/>
    <corners android:radius="12dp"/>
</shape>
EOF
cat > app/src/main/res/layout/fragment_remote.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/transparent"
    android:fitsSystemWindows="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:background="@android:color/transparent">
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="20dp"
            android:background="#55FFFFFF">
            <TextView
                android:id="@+id/remote_title_text"
                android:tag="page_title"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/nav_remote"
                android:textSize="26sp"
                android:textStyle="bold"
                android:textColor="@color/text_dark"
                android:layout_centerVertical="true"/>
        </RelativeLayout>
        <androidx.core.widget.NestedScrollView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:clipToPadding="false"
            android:background="@android:color/transparent">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:paddingLeft="16dp"
                android:paddingRight="16dp"
                android:paddingTop="16dp"
                android:paddingBottom="110dp">
                <HorizontalScrollView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:scrollbars="none"
                    android:layout_marginBottom="10dp">
                    <LinearLayout
                        android:id="@+id/ll_remote_devices"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:visibility="gone"/>
                </HorizontalScrollView>
                <androidx.cardview.widget.CardView
                    android:id="@+id/card_remote_conn"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="14dp"
                    app:cardCornerRadius="16dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="#CCFFFFFF">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="18dp">
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:layout_marginBottom="14dp">
                            <TextView
                                android:id="@+id/btn_mode_wireless"
                                android:layout_width="0dp"
                                android:layout_height="46dp"
                                android:layout_weight="1"
                                android:layout_marginEnd="6dp"
                                android:gravity="center"
                                android:text="@string/remote_mode_wireless"
                                android:textSize="14sp"
                                android:textStyle="bold"
                                android:textColor="#FFFFFFFF"
                                android:background="@drawable/bg_tab_selected"/>
                            <TextView
                                android:id="@+id/btn_mode_usb"
                                android:layout_width="0dp"
                                android:layout_height="46dp"
                                android:layout_weight="1"
                                android:gravity="center"
                                android:text="@string/remote_mode_usb"
                                android:textSize="14sp"
                                android:textStyle="bold"
                                android:textColor="#546E7A"
                                android:background="@drawable/bg_tab_unselected"/>
                        </LinearLayout>
                        <LinearLayout
                            android:id="@+id/ll_wireless_inputs"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical">
                            <EditText
                                android:id="@+id/et_remote_ip"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:hint="@string/remote_ip_hint"
                                android:inputType="text"
                                android:textSize="15sp"/>
                            <LinearLayout
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:orientation="horizontal"
                                android:gravity="center_vertical"
                                android:layout_marginTop="6dp">
                                <TextView
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                                    android:text="@string/remote_blur_ip"
                                    android:textSize="13sp"
                                    android:textColor="@color/text_muted"/>
                                <androidx.appcompat.widget.SwitchCompat
                                    android:id="@+id/switch_blur_ip"
                                    app:track="@drawable/switch_track_md3"
                                    android:switchMinWidth="33dp"
                                    android:thumbTextPadding="0dp"
                                    app:thumbTint="@null"
                                    app:trackTint="@null"
                                    android:thumb="@drawable/switch_thumb_md3"
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"/>
                            </LinearLayout>
                            <TextView
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="12dp"
                                android:text="@string/remote_section_pair"
                                android:textSize="13sp"
                                android:textStyle="bold"
                                android:textColor="@color/text_dark"/>
                            <EditText
                                android:id="@+id/et_remote_pair_port"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:hint="@string/remote_pair_port_hint"
                                android:inputType="number"
                                android:textSize="15sp"/>
                            <EditText
                                android:id="@+id/et_remote_pair"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:hint="@string/remote_pair_hint"
                                android:inputType="number"
                                android:textSize="15sp"/>
                            <com.google.android.material.button.MaterialButton
                                android:id="@+id/btn_remote_pair"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="8dp"
                                android:text="@string/remote_pair_btn"
                                android:textColor="#1976D2"
                                app:backgroundTint="#E3F2FD"
                                app:cornerRadius="24dp"
                                app:elevation="0dp"/>
                            <TextView
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="14dp"
                                android:text="@string/remote_section_connect"
                                android:textSize="13sp"
                                android:textStyle="bold"
                                android:textColor="@color/text_dark"/>
                            <EditText
                                android:id="@+id/et_remote_port"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:hint="@string/remote_port_hint"
                                android:inputType="number"
                                android:textSize="15sp"/>
                            <com.google.android.material.button.MaterialButton
                                android:id="@+id/btn_remote_connect"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="8dp"
                                android:text="@string/remote_connect_btn"
                                android:textColor="#FFFFFF"
                                app:backgroundTint="#1976D2"
                                app:cornerRadius="24dp"
                                app:elevation="0dp"/>
                            <TextView
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="10dp"
                                android:text="@string/remote_hint"
                                android:textSize="12sp"
                                android:textColor="@color/text_muted"/>
                            <TextView
                                android:id="@+id/tv_history_title"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="14dp"
                                android:text="@string/remote_history_title"
                                android:textSize="13sp"
                                android:textStyle="bold"
                                android:textColor="@color/text_dark"
                                android:visibility="gone"/>
                            <LinearLayout
                                android:id="@+id/ll_remote_history"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:orientation="vertical"
                                android:visibility="gone"/>
                        </LinearLayout>
                        <LinearLayout
                            android:id="@+id/ll_usb_hint"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"
                            android:visibility="gone">
                            <TextView
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:text="@string/remote_usb_hint"
                                android:textSize="13sp"
                                android:textColor="@color/text_muted"/>
                        </LinearLayout>
                        <TextView
                            android:id="@+id/tv_remote_status"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="14dp"
                            android:gravity="center"
                            android:text="@string/remote_status_idle"
                            android:textSize="13sp"
                            android:textColor="@color/text_muted"/>
                    </LinearLayout>
                </androidx.cardview.widget.CardView>
                <androidx.cardview.widget.CardView
                    android:id="@+id/card_remote_modes"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    app:cardCornerRadius="16dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="#CCFFFFFF">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:paddingTop="18dp"
                        android:paddingBottom="10dp">
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginStart="18dp"
                            android:text="@string/remote_modes_title"
                            android:textSize="16sp"
                            android:textStyle="bold"
                            android:textColor="@color/text_dark"/>
                        <TextView
                            android:id="@+id/tv_remote_device"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginStart="18dp"
                            android:layout_marginTop="3dp"
                            android:textSize="13sp"
                            android:textColor="@color/text_muted"
                            android:visibility="gone"/>
                        <androidx.recyclerview.widget.RecyclerView
                            android:id="@+id/recycler_remote_modes"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:nestedScrollingEnabled="false"
                            android:background="@android:color/transparent"/>
                        <TextView
                            android:id="@+id/tv_remote_modes_empty"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="16dp"
                            android:layout_marginBottom="8dp"
                            android:gravity="center"
                            android:text="@string/remote_status_none"
                            android:textSize="14sp"
                            android:textColor="@color/text_muted"/>
                    </LinearLayout>
                </androidx.cardview.widget.CardView>
            </LinearLayout>
        </androidx.core.widget.NestedScrollView>
    </LinearLayout>
</FrameLayout>
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/fragment/HomeFragment.java << 'EOF'
package com.yxlanyu.refreshrate.fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.adapter.RateAdapter;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import com.yxlanyu.refreshrate.util.RootUtils;
import java.util.*;
public class HomeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private RateAdapter adapter;
    private boolean hasRoot = false;
    private List<Object> dataItems = new ArrayList<>();
    private RecyclerView recyclerView;
    private SharedPreferences prefs;
    private View titleBar;
    private TextView titleText;
    private ImageView btnLanguage;
    private PopupWindow languagePopup;
    @Override public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle s) {
        View v = inf.inflate(R.layout.fragment_home, c, false);
        recyclerView = v.findViewById(R.id.recycler_rates);
        titleBar = v.findViewById(R.id.home_title_bar);
        titleText = v.findViewById(R.id.home_title_text);
        titleText.setText(R.string.home_title);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(null);
        adapter = new RateAdapter(getContext(), dataItems, this::onRateSelected);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (!(getActivity() instanceof com.yxlanyu.refreshrate.MainActivity)) return;
                com.yxlanyu.refreshrate.MainActivity ma = (com.yxlanyu.refreshrate.MainActivity) getActivity();
                if (dy > 8) ma.hideBottomNav();
                else if (dy < -8) ma.showBottomNav();
            }
        });
        prefs = requireActivity().getSharedPreferences("s", Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(this);
        updateUI();
        loadData();
        return v;
    }
    private int[] getCurrentWHRate() {
        try {
            android.hardware.display.DisplayManager dm = (android.hardware.display.DisplayManager)
                    requireActivity().getSystemService(Context.DISPLAY_SERVICE);
            android.view.Display d = dm.getDisplay(android.view.Display.DEFAULT_DISPLAY);
            android.view.Display.Mode m = d.getMode();
            return new int[]{ m.getPhysicalWidth(), m.getPhysicalHeight(), Math.round(d.getRefreshRate()) };
        } catch (Exception e) { return new int[]{0, 0, 0}; }
    }
    private void showLanguagePopup() {
        if (languagePopup != null && languagePopup.isShowing()) {
            languagePopup.dismiss();
        }
        String[] languages = {
            getString(R.string.language_zh),
            getString(R.string.language_zh_tw),
            getString(R.string.language_en),
            getString(R.string.language_ja)
        };
        String[] langCodes = {LanguageUtils.LANG_ZH, LanguageUtils.LANG_ZH_TW, LanguageUtils.LANG_EN, LanguageUtils.LANG_JA};
        String currentLang = LanguageUtils.getCurrentLang(getContext());
        ListView listView = new ListView(getContext());
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_single_choice, languages);
        listView.setAdapter(listAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                listView.setItemChecked(i, true);
                break;
            }
        }
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLang = langCodes[position];
            if (!selectedLang.equals(LanguageUtils.getCurrentLang(getContext()))) {
                LanguageUtils.setLanguageAndRecreate(requireActivity(), selectedLang);
            }
            languagePopup.dismiss();
        });
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(16));
        bg.setColor(Color.WHITE);
        listView.setBackground(bg);
        listView.setClipToOutline(true);
        int width = (int) (150 * getResources().getDisplayMetrics().density);
        languagePopup = new PopupWindow(listView, width, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        languagePopup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        languagePopup.setElevation(12);
        languagePopup.showAsDropDown(btnLanguage, 0, 8);
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    private void updateUI() {
        float wallpaperAlpha = prefs.getFloat("wallpaper_alpha", 0.3f);
        float titleAlpha = 1f - wallpaperAlpha;
        titleBar.setAlpha(titleAlpha);
        titleText.setAlpha(titleAlpha);
        int cardAlpha = prefs.getInt("card_alpha", 0);
        float cardAlphaF = 1f - (cardAlpha / 100f);
        recyclerView.setAlpha(cardAlphaF);
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("wallpaper_alpha".equals(key) || "card_alpha".equals(key) || "home_card_alpha".equals(key)) {
            updateUI();
        }
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        if (languagePopup != null) {
            languagePopup.dismiss();
            languagePopup = null;
        }
    }
    private void loadData() {
        new Thread(() -> {
            hasRoot = RootUtils.isRooted();
            List<DisplayMode> modes;
            if (hasRoot) {
            List<DisplayMode> dumpsysModes = RootUtils.getDisplayModesFromDumpsys();
            if (dumpsysModes.isEmpty()) hasRoot = false;
            }
            List<Object> built = buildSortedList(modes);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    dataItems.clear();
                    dataItems.addAll(built);
                    adapter.notifyDataSetChanged();
                    int selW = prefs.getInt("last_sel_w", 0);
                    int selH = prefs.getInt("last_sel_h", 0);
                    int selHz = prefs.getInt("last_sel_hz", 0);
                    if (selHz > 0) {
                        adapter.setCurrentByWHR(selW, selH, selHz);
                    } else {
                        int[] cur = getCurrentWHRate();
                        if (cur[2] > 0) adapter.setCurrentByWHR(cur[0], cur[1], cur[2]);
                    }
                });
            }
        }).start();
    }
    private void onRateSelected(DisplayMode mode) {
        String authMode = prefs.getString("auth_mode", "");
        boolean useRoot    = "root".equals(authMode) && hasRoot;
        boolean useShizuku = "shizuku".equals(authMode)
                && com.yxlanyu.refreshrate.util.ShizukuUtils.isAvailable()
                && com.yxlanyu.refreshrate.util.ShizukuUtils.hasPermission();
        if (!useRoot && !useShizuku) {
            Toast.makeText(getContext(), R.string.no_root_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            boolean ok = false;
            if (useRoot) {
                ok = RootUtils.setDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRateInt(), mode.getSfIndex());
            } else if (useShizuku) {
                ok = com.yxlanyu.refreshrate.util.ShizukuUtils.setDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRateInt(), mode.getSfIndex());
            }
            final boolean success = ok;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        adapter.setCurrent(mode);
                        prefs.edit()
                            .putInt("last_sel_w", mode.getWidth())
                            .putInt("last_sel_h", mode.getHeight())
                            .putInt("last_sel_hz", mode.getRateInt())
                            .putInt("last_sel_sf", mode.getSfIndex())
                            .apply();
                        Toast.makeText(getContext(), getString(R.string.switch_success, mode.getRateInt()), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.switch_fail, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    private List<DisplayMode> getFallbackFromDisplayManager() {
        List<DisplayMode> list = new ArrayList<>();
        try {
            android.hardware.display.DisplayManager dm = (android.hardware.display.DisplayManager)
                    requireActivity().getSystemService(Context.DISPLAY_SERVICE);
            android.view.Display d = dm.getDisplay(android.view.Display.DEFAULT_DISPLAY);
            android.view.Display.Mode[] modes = d.getSupportedModes();
            int sfIdx = 0;
            for (android.view.Display.Mode m : modes) {
                DisplayMode dmode = new DisplayMode(m.getPhysicalWidth(), m.getPhysicalHeight(),
                        m.getRefreshRate(), m.getModeId());
                dmode.setSfIndex(sfIdx++);
                list.add(dmode);
            }
        } catch (Exception ignored) {}
        return list;
    }
    private List<Object> buildSortedList(List<DisplayMode> dumpedModes) {
        if (dumpedModes == null || dumpedModes.isEmpty()) return fallback();
        Map<String, List<DisplayMode>> grouped = new LinkedHashMap<>();
        for (DisplayMode m : dumpedModes) {
            String k = m.getWidth() + "x" + m.getHeight();
            grouped.computeIfAbsent(k, x -> new ArrayList<>()).add(m);
        }
        List<String> keys = new ArrayList<>(grouped.keySet());
        keys.sort((a, b) -> {
            String[] pa = a.split("x"), pb = b.split("x");
            int resA = Integer.parseInt(pa[0]) * Integer.parseInt(pa[1]);
            int resB = Integer.parseInt(pb[0]) * Integer.parseInt(pb[1]);
            return Integer.compare(resB, resA);
        });
        List<Object> items = new ArrayList<>();
        Context ctx = getContext();
        for (int i = 0; i < keys.size(); i++) {
            List<DisplayMode> ms = grouped.get(keys.get(i));
            ms.sort((a, b) -> Float.compare(b.getRefreshRate(), a.getRefreshRate()));
            String label = (i == 0) ?
                ctx.getString(R.string.high_resolution) + keys.get(i)
                                    : ctx.getString(R.string.low_resolution) + keys.get(i);
            items.add(label);
            items.addAll(ms);
        }
        return items;
    }
    private List<Object> fallback() {
        int[] highResRates = {60, 90, 120, 144, 165, 170, 175, 177};
        int[] lowResRates  = {60, 90, 120, 144, 165, 170, 175, 177};
        List<Object> l = new ArrayList<>();
        l.add(getString(R.string.fallback_high_res));
        for (int r : highResRates) l.add(new DisplayMode(1272, 2772, r, -1));
        l.add(getString(R.string.fallback_low_res));
        for (int r : lowResRates) l.add(new DisplayMode(1080, 2354, r, -1));
        return l;
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/LanguageActivity.java << 'EOF'
package com.yxlanyu.refreshrate;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.yxlanyu.refreshrate.util.LanguageUtils;
public class LanguageActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private final String[] codes = {
        LanguageUtils.LANG_SYSTEM, LanguageUtils.LANG_ZH, LanguageUtils.LANG_ZH_TW,
        LanguageUtils.LANG_EN, LanguageUtils.LANG_JA
    };
    private final int[] nameRes = {
        R.string.lang_system, R.string.lang_zh, R.string.lang_zh_tw,
        R.string.lang_en, R.string.lang_ja
    };
    private final String[] badges = {"文A", "简", "繁", "EN", "JA"};
    @Override protected void onCreate(Bundle s) {
        LanguageUtils.applyLanguage(this);
        super.onCreate(s);
        int pad = dp(16);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0x00000000);
        sv.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, dp(48), pad, pad);
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = new TextView(this);
        back.setText("\u2190");
        back.setTextSize(24);
        back.setTextColor(0xFF222222);
        back.setPadding(0, 0, dp(16), 0);
        back.setOnClickListener(v -> finish());
        TextView title = new TextView(this);
        title.setText(R.string.language_page_title);
        title.setTextSize(24);
        title.setTextColor(0xFF222222);
        title.getPaint().setFakeBoldText(true);
        header.addView(back);
        header.addView(title);
        root.addView(header);
        root.addView(space(dp(20)));
        String cur = LanguageUtils.getCurrentLang(this);
        for (int i = 0; i < codes.length; i++) {
            root.addView(buildRow(i, codes[i].equals(cur)));
            root.addView(space(dp(12)));
        }
        sv.addView(root);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.ImageView wp = new android.widget.ImageView(this);
        wp.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        wp.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        applyWallpaperBg(wp);
        container.addView(wp);
        container.addView(sv);
        setContentView(container);
    }
    private void applyWallpaperBg(android.widget.ImageView wp) {
        android.content.SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
        int type = prefs.getInt("wallpaper_type", 4);
        wp.setAlpha(prefs.getFloat("wallpaper_alpha", 0.3f));
        if (type == 3) {
            android.graphics.Bitmap bm = WallpaperStore.loadPage(this, "settings");
            if (bm != null) { wp.setImageBitmap(bm); return; }
            wp.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.wallpaper_white));
            return;
        }
        wp.setImageDrawable(WallpaperStore.presetDrawable(this, type));
    }
    private View buildRow(final int idx, boolean selected) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(64));
        card.setLayoutParams(lp);
        card.setRadius(dp(18));
        card.setCardElevation(0f);
        int baseColor = selected ? 0xFFD9D7F5 : 0xFFE9E8F2;
        int cardAlphaPref = getSharedPreferences("s", MODE_PRIVATE).getInt("setting_card_alpha", 0);
        int a = 255 - (int) (cardAlphaPref / 100f * 255);
        if (a < 0) a = 0; if (a > 255) a = 255;
        card.setCardBackgroundColor((a << 24) | (baseColor & 0x00FFFFFF));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), 0, dp(18), 0);
        TextView badge = new TextView(this);
        badge.setText(badges[idx]);
        badge.setTextColor(0xFF555555);
        badge.setTextSize(13);
        badge.setGravity(Gravity.CENTER);
        android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
        circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        circle.setColor(0xFFD8D7E2);
        badge.setBackground(circle);
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(dp(40), dp(40));
        bl.rightMargin = dp(16);
        badge.setLayoutParams(bl);
        TextView name = new TextView(this);
        name.setText(nameRes[idx]);
        name.setTextSize(18);
        name.setTextColor(0xFF222222);
        row.addView(badge);
        row.addView(name);
        card.addView(row);
        card.setOnClickListener(v -> {
            LanguageUtils.setLanguageAndRecreate(LanguageActivity.this, codes[idx]);
        });
        return card;
    }
    private View space(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, h));
        return v;
    }
    private int dp(float d) { return (int) (d * getResources().getDisplayMetrics().density); }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/AppearanceActivity.java << 'EOF'
package com.yxlanyu.refreshrate;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import java.io.InputStream;
public class AppearanceActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private SharedPreferences prefs;
    private ImageView wallpaperView;
    private String pendingPageKey;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        LanguageUtils.applyLanguage(this);
        setContentView(R.layout.activity_appearance);
        prefs = getSharedPreferences("s", MODE_PRIVATE);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_appearance);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        wallpaperView = findViewById(R.id.appearance_wallpaper);
        applyWallpaper();
        setupNavSchemeRow();
        setupWallpaper();
        setupCardDisplay();
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null && pendingPageKey != null) {
                        boolean saved = WallpaperStore.savePageFromUri(this, uri, pendingPageKey);
                        if (saved) {
                            prefs.edit().putInt("wallpaper_type", 3).apply();
                            refreshPresetCards();
                            updateCustomBgVisibility();
                            refreshAllBgCards();
                            applyWallpaper();
                            Toast.makeText(this, R.string.custom_wallpaper_saved, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.update_check_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        applyWallpaper();
    }
    private void applyWallpaper() {
        if (wallpaperView == null) return;
        int type = prefs.getInt("wallpaper_type", 4);
        float alpha = prefs.getFloat("wallpaper_alpha", 0.3f);
        wallpaperView.setAlpha(alpha);
        if (type == 3) {
            android.graphics.Bitmap bm = WallpaperStore.loadPage(this, "settings");
            if (bm != null) { wallpaperView.setImageBitmap(bm); return; }
            wallpaperView.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.wallpaper_white));
            return;
        }
        wallpaperView.setImageDrawable(WallpaperStore.presetDrawable(this, type));
    }
    private void setupNavSchemeRow() {
        TextView tvCurrent = findViewById(R.id.tv_nav_style_current);
        if (tvCurrent == null) return;
        updateNavSchemeText(tvCurrent);
        android.view.View row = findViewById(R.id.row_nav_style);
        if (row != null) row.setOnClickListener(v -> showNavSchemeDialog(tvCurrent));
    }
    private void updateNavSchemeText(TextView tv) {
        int scheme = prefs.getInt("nav_scheme", 0);
        int[] names = { R.string.nav_scheme_float, R.string.nav_scheme_classic, R.string.nav_scheme_top, R.string.nav_scheme_left, R.string.nav_scheme_right };
        if (scheme < 0 || scheme >= names.length) scheme = 0;
        tv.setText(getString(names[scheme]));
    }
    private void showNavSchemeDialog(TextView tvCurrent) {
    int current = prefs.getInt("nav_scheme", 0);
    android.view.View dialogView = android.view.LayoutInflater.from(this)
        .inflate(R.layout.dialog_nav_scheme, null);
    android.widget.RadioGroup rg = dialogView.findViewById(R.id.rg_nav_scheme);
    int[] rbIds = { R.id.rb_nav_float, R.id.rb_nav_classic, R.id.rb_nav_top, R.id.rb_nav_left, R.id.rb_nav_right };
    rg.check(rbIds[(current >= 0 && current < rbIds.length) ? current : 0]);
    AlertDialog dialog = new AlertDialog.Builder(this)
        .setView(dialogView)
        .create();
    if (dialog.getWindow() != null) {
        dialog.getWindow().setBackgroundDrawable(
            new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
    }
    rg.setOnCheckedChangeListener((group, checkedId) -> {
        int which = checkedId == R.id.rb_nav_classic ? 1 : checkedId == R.id.rb_nav_top ? 2 : checkedId == R.id.rb_nav_left ? 3 : checkedId == R.id.rb_nav_right ? 4 : 0;
        prefs.edit().putInt("nav_scheme", which).apply();
        updateNavSchemeText(tvCurrent);
        dialog.dismiss();
    });
    dialog.show();
}
    private void setupWallpaper() {
        SeekBar sbAlpha = findViewById(R.id.sb_alpha);
        final android.widget.TextView tvAlphaPct = findViewById(R.id.tv_alpha_pct);
        if (sbAlpha == null) return;
        int initP = (int)(prefs.getFloat("wallpaper_alpha", 0.3f) * 100);
        sbAlpha.setProgress(initP);
        if (tvAlphaPct != null) tvAlphaPct.setText(initP + "%");
        sbAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                float alpha = p / 100f;
                prefs.edit().putFloat("wallpaper_alpha", alpha).apply();
                if (wallpaperView != null) wallpaperView.setAlpha(alpha);
                if (tvAlphaPct != null) tvAlphaPct.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) { applyWallpaper(); }
        });
        int[][] cards = {{R.id.wp_custom,3},{R.id.wp_white,4},{R.id.wp_black,5},{R.id.wp_sunset,6},{R.id.wp_aurora,1},{R.id.wp_emerald,2},{R.id.wp_sky,7},{R.id.wp_pink,8},{R.id.wp_mint,9},{R.id.wp_purple,10}};
        for (int[] c : cards) {
            final int t = c[1];
            android.view.View v = findViewById(c[0]);
            if (v != null) v.setOnClickListener(x -> selectWpType(t));
        }
        refreshPresetCards();
        setupBgCard("home", R.id.iv_bg_home, R.id.btn_clear_home, R.id.btn_pick_home);
        setupBgCard("app", R.id.iv_bg_app, R.id.btn_clear_app, R.id.btn_pick_app);
        setupBgCard("remote", R.id.iv_bg_remote, R.id.btn_clear_remote, R.id.btn_pick_remote);
        setupBgCard("settings", R.id.iv_bg_settings, R.id.btn_clear_settings, R.id.btn_pick_settings);
        updateCustomBgVisibility();
    }
    private void selectWpType(int type) {
        prefs.edit().putInt("wallpaper_type", type).apply();
        refreshPresetCards();
        updateCustomBgVisibility();
        applyWallpaper();
    }
    private void refreshPresetCards() {
        int cur = prefs.getInt("wallpaper_type", 4);
        styleCard(R.id.wp_custom, cur == 3);
        styleCard(R.id.wp_white, cur == 4);
        styleCard(R.id.wp_black, cur == 5);
        styleCard(R.id.wp_sunset, cur == 6);
        styleCard(R.id.wp_aurora, cur == 1);
        styleCard(R.id.wp_emerald, cur == 2);
        styleCard(R.id.wp_sky, cur == 7);
        styleCard(R.id.wp_pink, cur == 8);
        styleCard(R.id.wp_mint, cur == 9);
        styleCard(R.id.wp_purple, cur == 10);
    }
    private void styleCard(int id, boolean sel) {
        android.widget.TextView tv = findViewById(id);
        if (tv == null) return;
        tv.setBackgroundResource(sel ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tv.setTextColor(sel ? 0xFFFFFFFF : 0xFF546E7A);
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, sel ? R.drawable.ic_check_white : 0, 0);
    }
    private void setupBgCard(String key, int ivId, int clearId, int pickId) {
        android.widget.ImageView iv = findViewById(ivId);
        android.view.View clear = findViewById(clearId);
        android.view.View pick = findViewById(pickId);
        refreshBgCard(key, iv, clear);
        if (pick != null) pick.setOnClickListener(v -> { pendingPageKey = key; pickPageImage(); });
        if (clear != null) clear.setOnClickListener(v -> { WallpaperStore.clearPage(this, key); refreshBgCard(key, iv, clear); });
    }
    private void refreshBgCard(String key, android.widget.ImageView iv, android.view.View clear) {
        boolean has = WallpaperStore.hasPage(this, key);
        if (iv != null) {
            if (has) { iv.setImageBitmap(WallpaperStore.loadPage(this, key)); iv.setVisibility(android.view.View.VISIBLE); }
            else iv.setVisibility(android.view.View.GONE);
        }
        if (clear != null) clear.setVisibility(has ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    private void refreshAllBgCards() {
        refreshBgCard("home", findViewById(R.id.iv_bg_home), findViewById(R.id.btn_clear_home));
        refreshBgCard("app", findViewById(R.id.iv_bg_app), findViewById(R.id.btn_clear_app));
        refreshBgCard("remote", findViewById(R.id.iv_bg_remote), findViewById(R.id.btn_clear_remote));
        refreshBgCard("settings", findViewById(R.id.iv_bg_settings), findViewById(R.id.btn_clear_settings));
    }
    private void updateCustomBgVisibility() {
        android.view.View ll = findViewById(R.id.ll_custom_bg);
        if (ll != null) ll.setVisibility(prefs.getInt("wallpaper_type", 4) == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    private void pickPageImage() {
        String perm = Build.VERSION.SDK_INT >= 33
            ? Manifest.permission.READ_MEDIA_IMAGES
            : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{perm}, 100);
        } else {
            openGallery();
        }
    }
    private void setupCardDisplay() {
        SeekBar sbCard = findViewById(R.id.sb_card_alpha);
        if (sbCard == null) return;
        final android.widget.TextView pctCard = findViewById(R.id.tv_pct_card);
        int initCard = prefs.getInt("card_alpha", 0);
        sbCard.setProgress(initCard);
        if (pctCard != null) pctCard.setText(initCard + "%");
        sbCard.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                prefs.edit().putInt("card_alpha", p).apply();
                if (pctCard != null) pctCard.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        SeekBar sbSettingCard = findViewById(R.id.sb_setting_card_alpha);
        SeekBar sbSettingText = findViewById(R.id.sb_setting_text_alpha);
        final android.widget.TextView pctSettingCard = findViewById(R.id.tv_pct_setting_card);
        final android.widget.TextView pctSettingText = findViewById(R.id.tv_pct_setting_text);
        applySettingCardPreview(
            prefs.getInt("setting_card_alpha", 0),
            prefs.getInt("setting_text_alpha", 0));
        if (sbSettingCard != null) {
            int initSC = prefs.getInt("setting_card_alpha", 0);
            sbSettingCard.setProgress(initSC);
            if (pctSettingCard != null) pctSettingCard.setText(initSC + "%");
            sbSettingCard.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                    int textP = sbSettingText != null ? sbSettingText.getProgress() : prefs.getInt("setting_text_alpha", 0);
                    prefs.edit().putInt("setting_card_alpha", p).apply();
                    applySettingCardPreview(p, textP);
                    if (pctSettingCard != null) pctSettingCard.setText(p + "%");
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }
        if (sbSettingText != null) {
            int initST = prefs.getInt("setting_text_alpha", 0);
            sbSettingText.setProgress(initST);
            if (pctSettingText != null) pctSettingText.setText(initST + "%");
            sbSettingText.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                    int cardP = sbSettingCard != null ? sbSettingCard.getProgress() : prefs.getInt("setting_card_alpha", 0);
                    prefs.edit().putInt("setting_text_alpha", p).apply();
                    applySettingCardPreview(cardP, p);
                    if (pctSettingText != null) pctSettingText.setText(p + "%");
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }
        bindAlphaSeek(R.id.sb_remote_card_alpha, "remote_card_alpha", R.id.tv_pct_remote_card);
        bindAlphaSeek(R.id.sb_remote_text_alpha, "remote_text_alpha", R.id.tv_pct_remote_text);
        bindAlphaSeek(R.id.sb_custom_card_alpha, "custom_card_alpha", R.id.tv_pct_custom_card);
        bindAlphaSeek(R.id.sb_custom_text_alpha, "custom_text_alpha", R.id.tv_pct_custom_text);
    }
    private void bindAlphaSeek(int id, final String key, int pctId) {
        SeekBar sb = findViewById(id);
        if (sb == null) return;
        final android.widget.TextView pct = findViewById(pctId);
        int init = prefs.getInt(key, 0);
        sb.setProgress(init);
        if (pct != null) pct.setText(init + "%");
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                prefs.edit().putInt(key, p).apply();
                if (pct != null) pct.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }
    private void applySettingCardPreview(int cardProgress, int textProgress) {
        int cardAlpha = 255 - (int)(cardProgress / 100f * 255);
        int textAlpha = 255 - (int)(textProgress / 100f * 255);
        android.view.View content = findViewById(android.R.id.content);
        if (content instanceof android.view.ViewGroup) {
            applyAlphaToViewGroup((android.view.ViewGroup) content, cardAlpha, textAlpha);
        }
    }
    private void applyAlphaToViewGroup(android.view.ViewGroup parent, int cardAlpha, int textAlpha) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child instanceof androidx.cardview.widget.CardView) {
                androidx.cardview.widget.CardView cv = (androidx.cardview.widget.CardView) child;
                cv.setCardBackgroundColor(android.graphics.Color.argb(cardAlpha, 255, 255, 255));
                cv.setCardElevation(0f);
                cv.setMaxCardElevation(0f);
            } else if (child instanceof android.widget.TextView) {
                child.setAlpha(textAlpha / 255f);
            }
            if (child instanceof android.view.ViewGroup) {
                applyAlphaToViewGroup((android.view.ViewGroup) child, cardAlpha, textAlpha);
            }
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                if (!shouldShowRequestPermissionRationale(permissions[0])) {
                    new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_denied_title)
                        .setMessage(R.string.permission_denied_message)
                        .setPositiveButton(R.string.permission_go_settings, (d, w) -> {
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            i.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(i);
                        })
                        .setNegativeButton(R.string.permission_cancel, null)
                        .show();
                } else {
                    Toast.makeText(this, R.string.need_storage_permission, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/AnimationActivity.java << 'EOF'
package com.yxlanyu.refreshrate;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
public class AnimationActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private interface OnPick { void pick(int index); }
    @Override protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        com.yxlanyu.refreshrate.util.LanguageUtils.applyLanguage(this);
        setContentView(R.layout.activity_animation);
        applyWallpaperBg((android.widget.ImageView) findViewById(R.id.anim_wallpaper));
        applyCardAlpha((androidx.cardview.widget.CardView) findViewById(R.id.anim_card));
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_animation);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(x -> finish());
        final android.content.SharedPreferences sp = getSharedPreferences("s", MODE_PRIVATE);
        androidx.appcompat.widget.SwitchCompat swAnim = findViewById(R.id.switch_anim_engine);
        if (swAnim != null) {
            swAnim.setChecked(sp.getBoolean("anim_engine", true));
            swAnim.setOnCheckedChangeListener((b, on) -> sp.edit().putBoolean("anim_engine", on).apply());
        }
        final String[] typeKeys = {"spring", "linear", "fade", "slide"};
        final String[] typeNames = { getString(R.string.anim_type_spring), getString(R.string.anim_type_linear), getString(R.string.anim_type_fade), getString(R.string.anim_type_slide) };
        final android.widget.TextView tvType = findViewById(R.id.tv_anim_type_value);
        final android.view.View rowType = findViewById(R.id.row_anim_type);
        if (tvType != null && rowType != null) {
            String cur = sp.getString("anim_type", "spring");
            int ci = 0; for (int i = 0; i < typeKeys.length; i++) if (typeKeys[i].equals(cur)) ci = i;
            tvType.setText(typeNames[ci]);
            final int[] sel = { ci };
            rowType.setOnClickListener(x -> showStyledChoice(getString(R.string.anim_type_title), typeNames, sel[0], idx -> {
                sel[0] = idx; sp.edit().putString("anim_type", typeKeys[idx]).apply(); tvType.setText(typeNames[idx]);
            }));
        }
        final float[] spd = {0.5f, 0.75f, 1.0f, 1.5f, 2.0f};
        final String[] spdNames = {"0.5x", "0.75x", "1.0x", "1.5x", "2.0x"};
        final android.widget.TextView tvSpd = findViewById(R.id.tv_anim_speed_value);
        final android.view.View rowSpd = findViewById(R.id.row_anim_speed);
        if (tvSpd != null && rowSpd != null) {
            float curs = sp.getFloat("anim_speed", 1.0f);
            int ci = 2; for (int i = 0; i < spd.length; i++) if (Math.abs(spd[i] - curs) < 0.01f) ci = i;
            tvSpd.setText(spdNames[ci]);
            final int[] sel = { ci };
            rowSpd.setOnClickListener(x -> showStyledChoice(getString(R.string.anim_speed_title), spdNames, sel[0], idx -> {
                sel[0] = idx; sp.edit().putFloat("anim_speed", spd[idx]).apply(); tvSpd.setText(spdNames[idx]);
            }));
        }
    }
    private void showStyledChoice(CharSequence title, CharSequence[] names, int current, OnPick cb) {
        android.view.View v = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_single_choice, null);
        ((android.widget.TextView) v.findViewById(R.id.dialog_choice_title)).setText(title);
        android.widget.RadioGroup rg = v.findViewById(R.id.dialog_choice_group);
        float d = getResources().getDisplayMetrics().density;
        for (int i = 0; i < names.length; i++) {
            android.widget.RadioButton rb = new android.widget.RadioButton(this);
            rb.setId(i + 1);
            rb.setText(names[i]);
            rb.setTextSize(17);
            rb.setTextColor(0xFF1A1A2E);
            rb.setPadding((int) (8 * d), 0, 0, 0);
            rb.setGravity(android.view.Gravity.CENTER_VERTICAL);
            rb.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFF1976D2));
            rg.addView(rb, new android.widget.RadioGroup.LayoutParams(android.widget.RadioGroup.LayoutParams.MATCH_PARENT, (int) (60 * d)));
        }
        rg.check(current + 1);
        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this).setView(v).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        rg.setOnCheckedChangeListener((g, id) -> { cb.pick(id - 1); dialog.dismiss(); });
        dialog.show();
    }
    private void applyCardAlpha(androidx.cardview.widget.CardView card) {
        if (card == null) return;
        int pref = getSharedPreferences("s", MODE_PRIVATE).getInt("setting_card_alpha", 0);
        card.setCardBackgroundColor(((255 - (int) (pref / 100f * 255)) << 24) | 0x00FFFFFF);
    }
    private void applyWallpaperBg(android.widget.ImageView wp) {
        if (wp == null) return;
        android.content.SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
        int type = prefs.getInt("wallpaper_type", 4);
        wp.setAlpha(prefs.getFloat("wallpaper_alpha", 0.3f));
        if (type == 3) {
            android.graphics.Bitmap bm = WallpaperStore.loadPage(this, "settings");
            if (bm != null) { wp.setImageBitmap(bm); return; }
            wp.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.wallpaper_white));
            return;
        }
        wp.setImageDrawable(WallpaperStore.presetDrawable(this, type));
    }
    @Override public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
EOF
cat > app/src/main/res/layout/activity_animation.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:fitsSystemWindows="true">
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar_animation"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="#F0F4FF"
        app:navigationIcon="@drawable/ic_back_arrow"
        app:title="@string/anim_effect_title"
        app:titleTextColor="#1A1A2E"/>
    <FrameLayout android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1">
        <ImageView android:id="@+id/anim_wallpaper" android:layout_width="match_parent" android:layout_height="match_parent" android:scaleType="centerCrop" android:alpha="0.3"/>
        <ScrollView android:layout_width="match_parent" android:layout_height="match_parent" android:background="@android:color/transparent">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">
            <androidx.cardview.widget.CardView android:id="@+id/anim_card" android:layout_width="match_parent"
                android:layout_height="wrap_content" android:layout_marginBottom="16dp"
                app:cardCornerRadius="12dp" app:cardBackgroundColor="#FFFFFF"
                app:cardElevation="0dp">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
                    android:orientation="vertical">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="64dp"
                        android:orientation="horizontal" android:gravity="center_vertical"
                        android:paddingStart="16dp" android:paddingEnd="16dp">
                        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
                            android:layout_weight="1" android:orientation="vertical">
                            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                                android:text="@string/anim_engine_title" android:textSize="16sp"
                                android:textStyle="bold" android:textColor="@color/text_dark"/>
                            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                                android:text="@string/anim_engine_desc" android:textSize="12sp"
                                android:textColor="@color/text_muted"/>
                        </LinearLayout>
                        <androidx.appcompat.widget.SwitchCompat android:id="@+id/switch_anim_engine"
                            app:track="@drawable/switch_track_md3" android:switchMinWidth="33dp"
                            android:thumbTextPadding="0dp" app:thumbTint="@null" app:trackTint="@null"
                            android:thumb="@drawable/switch_thumb_md3"
                            android:layout_width="wrap_content" android:layout_height="wrap_content"/>
                    </LinearLayout>
                    <LinearLayout android:id="@+id/row_anim_type" android:layout_width="match_parent"
                        android:layout_height="64dp" android:orientation="horizontal" android:gravity="center_vertical"
                        android:paddingStart="16dp" android:paddingEnd="16dp"
                        android:clickable="true" android:focusable="true" android:background="?attr/selectableItemBackground">
                        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
                            android:layout_weight="1" android:orientation="vertical">
                            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                                android:text="@string/anim_type_title" android:textSize="16sp"
                                android:textStyle="bold" android:textColor="@color/text_dark"/>
                            <TextView android:id="@+id/tv_anim_type_value" android:layout_width="wrap_content"
                                android:layout_height="wrap_content" android:textSize="12sp"
                                android:textColor="@color/text_muted"/>
                        </LinearLayout>
                        <ImageView android:layout_width="20dp" android:layout_height="20dp"
                            android:src="@drawable/ic_chevron_right" android:alpha="0.5"/>
                    </LinearLayout>
                    <LinearLayout android:id="@+id/row_anim_speed" android:layout_width="match_parent"
                        android:layout_height="64dp" android:orientation="horizontal" android:gravity="center_vertical"
                        android:paddingStart="16dp" android:paddingEnd="16dp"
                        android:clickable="true" android:focusable="true" android:background="?attr/selectableItemBackground">
                        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
                            android:layout_weight="1" android:orientation="vertical">
                            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                                android:text="@string/anim_speed_title" android:textSize="16sp"
                                android:textStyle="bold" android:textColor="@color/text_dark"/>
                            <TextView android:id="@+id/tv_anim_speed_value" android:layout_width="wrap_content"
                                android:layout_height="wrap_content" android:textSize="12sp"
                                android:textColor="@color/text_muted"/>
                        </LinearLayout>
                        <ImageView android:layout_width="20dp" android:layout_height="20dp"
                            android:src="@drawable/ic_chevron_right" android:alpha="0.5"/>
                    </LinearLayout>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
        </LinearLayout>
        </ScrollView>
    </FrameLayout>
</LinearLayout>
EOF
cat > app/src/main/java/com/yxlanyu/refreshrate/fragment/CustomAppRefreshFragment.java << 'EOF'
package com.yxlanyu.refreshrate.fragment;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import com.yxlanyu.refreshrate.AppListActivity;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
import com.yxlanyu.refreshrate.util.AccessibilityUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
public class CustomAppRefreshFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences prefs;
    private View titleBar;
    private TextView titleText;
    private TextView tvOCStatus;
    private TextView tvOCCur;
    private TextView tvA11yStatus;
    private View ocTargetContainer;
    private Handler overclockHandler;
    private Runnable overclockUpdater;
    private android.widget.LinearLayout llEnabledApps;
    private android.widget.TextView tvEnabledEmpty;
    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle s) {
        View v = inf.inflate(R.layout.fragment_custom_app_refresh, c, false);
        titleBar = v.findViewById(R.id.custom_app_refresh_title_bar);
        titleText = v.findViewById(R.id.custom_app_refresh_title_text);
        tvOCStatus = v.findViewById(R.id.tv_overclock_status);
        tvOCCur = v.findViewById(R.id.tv_overclock_cur);
        tvA11yStatus = v.findViewById(R.id.tv_accessibility_status);
        ocTargetContainer = v.findViewById(R.id.ll_oc_target_container);
        llEnabledApps = v.findViewById(R.id.ll_enabled_apps);
        tvEnabledEmpty = v.findViewById(R.id.tv_enabled_empty);
        prefs = requireActivity().getSharedPreferences("s", Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(this);
        v.findViewById(R.id.row_app_management).setOnClickListener(view -> {
            try {
                startActivity(new Intent(requireContext(), AppListActivity.class));
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.accessibility_open_failed, Toast.LENGTH_SHORT).show();
            }
        });
        setupSwitchesAndTargets(v);
        updateUI();
        updateAccessibilityStatus();
        updateCurrentDisplayText();
        return v;
    }
    private void setupSwitchesAndTargets(View v) {
        SwitchCompat switchCustom = v.findViewById(R.id.switch_custom_app_refresh);
        SwitchCompat switchOC = v.findViewById(R.id.switch_auto_overclock);
        Spinner spinnerRes = v.findViewById(R.id.spinner_oc_resolution);
        Spinner spinnerRate = v.findViewById(R.id.spinner_oc_rate);
        final boolean[] mute = {false};
        List<DisplayMode> allModes = AutoOverclockManager.getSupportedModes(requireContext());
        LinkedHashMap<String, List<DisplayMode>> resMap = new LinkedHashMap<>();
        for (DisplayMode m : allModes) {
            String key = m.getWidth() + "x" + m.getHeight();
            resMap.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }
        List<String> resKeys = new ArrayList<>(resMap.keySet());
        resKeys.sort((a, b) -> {
            try {
                String[] pa = a.split("x"), pb = b.split("x");
                return Integer.parseInt(pb[0]) * Integer.parseInt(pb[1]) - Integer.parseInt(pa[0]) * Integer.parseInt(pa[1]);
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });
        List<String> resLabels = new ArrayList<>();
        for (String k : resKeys) resLabels.add(k.replace("x", "×"));
        ArrayAdapter<String> resAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, resLabels);
        resAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRes.setAdapter(resAdapter);
        String savedResVal = prefs.getString("oc_target_res", resKeys.isEmpty() ? "" : resKeys.get(0));
        int savedResIdx = resKeys.indexOf(savedResVal);
        if (savedResIdx < 0) savedResIdx = 0;
        if (!resKeys.isEmpty()) {
            spinnerRes.setSelection(savedResIdx);
            if (prefs.getString("oc_target_res", "").isEmpty()) {
                prefs.edit().putString("oc_target_res", resKeys.get(savedResIdx)).apply();
            }
        }
        final List<Integer>[] rateHolder = new List[]{new ArrayList<>()};
        Runnable populateRates = () -> {
            int resIdx = spinnerRes.getSelectedItemPosition();
            if (resIdx < 0 || resIdx >= resKeys.size()) return;
            String rk = resKeys.get(resIdx);
            List<DisplayMode> ms = resMap.get(rk);
            if (ms == null) return;
            ms.sort((a, b) -> Float.compare(b.getRefreshRate(), a.getRefreshRate()));
            List<String> rateLabels = new ArrayList<>();
            rateHolder[0] = new ArrayList<>();
            for (DisplayMode m : ms) {
                rateLabels.add(m.getRateInt() + " Hz");
                rateHolder[0].add(m.getRateInt());
            }
            ArrayAdapter<String> ra = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, rateLabels);
            ra.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRate.setAdapter(ra);
            int savedHz = prefs.getInt("oc_target_hz", -1);
            int idx = rateHolder[0].indexOf(savedHz);
            if (!rateHolder[0].isEmpty()) {
                spinnerRate.setSelection(idx >= 0 ? idx : 0);
                if (savedHz < 0) prefs.edit().putInt("oc_target_hz", rateHolder[0].get(idx >= 0 ? idx : 0)).apply();
            }
        };
        populateRates.run();
        spinnerRes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, android.view.View vv, int pos, long id) {
                if (pos < 0 || pos >= resKeys.size()) return;
                prefs.edit().putString("oc_target_res", resKeys.get(pos)).apply();
                populateRates.run();
                updateRunningTarget(resKeys.get(pos), prefs.getInt("oc_target_hz", -1));
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        spinnerRate.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, android.view.View vv, int pos, long id) {
                if (pos < 0 || pos >= rateHolder[0].size()) return;
                int hz = rateHolder[0].get(pos);
                prefs.edit().putInt("oc_target_hz", hz).apply();
                updateRunningTarget(prefs.getString("oc_target_res", resKeys.isEmpty() ? "" : resKeys.get(0)), hz);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        mute[0] = true;
        boolean ocOn = prefs.getBoolean("auto_overclock", false) && AutoOverclockManager.isRunning();
        switchOC.setChecked(ocOn);
        switchCustom.setChecked(prefs.getBoolean("custom_app_refresh", false) && !ocOn);
        showOcTargetContainer(ocOn);
        if (ocOn) {
            tvOCStatus.setText(R.string.guard_enabled);
            tvOCStatus.setTextColor(0xFF2ECC71);
            startOverclockStatusUpdater();
        }
        mute[0] = false;
        switchCustom.setOnCheckedChangeListener((btn, checked) -> {
            if (mute[0]) return;
            if (checked) {
                if (!ensureBasePermission(switchCustom)) return;
                mute[0] = true;
                switchOC.setChecked(false);
                mute[0] = false;
                prefs.edit()
                        .putBoolean("custom_app_refresh", true)
                        .putBoolean("auto_overclock", false)
                        .apply();
                AutoOverclockManager.stopService(requireContext());
                stopOverclockStatusUpdater();
                tvOCStatus.setText(R.string.guard_disabled);
                tvOCStatus.setTextColor(0xFFE74C3C);
                showOcTargetContainer(false);
            } else {
                prefs.edit().putBoolean("custom_app_refresh", false).apply();
            }
        });
        switchOC.setOnCheckedChangeListener((btn, checked) -> {
            if (mute[0]) return;
            prefs.edit().putBoolean("auto_overclock", checked).apply();
            if (checked) {
                if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200);
                    mute[0] = true;
                    switchOC.setChecked(false);
                    mute[0] = false;
                    prefs.edit().putBoolean("auto_overclock", false).apply();
                    return;
                }
                if (!ensureBasePermission(switchOC)) return;
                String ocRes = prefs.getString("oc_target_res", resKeys.isEmpty() ? "" : resKeys.get(0));
                int ocHz = prefs.getInt("oc_target_hz", -1);
                if (ocRes.isEmpty() && !resKeys.isEmpty()) {
                    ocRes = resKeys.get(Math.max(0, spinnerRes.getSelectedItemPosition()));
                }
                if (ocHz < 0) {
                    try {
                        String lbl = (String) spinnerRate.getSelectedItem();
                        if (lbl != null) ocHz = Integer.parseInt(lbl.replace(" Hz", "").trim());
                    } catch (Exception ignored) {}
                }
                if (ocRes.isEmpty() || ocHz < 0) {
                    mute[0] = true;
                    switchOC.setChecked(false);
                    mute[0] = false;
                    prefs.edit().putBoolean("auto_overclock", false).apply();
                    return;
                }
                prefs.edit().putString("oc_target_res", ocRes).putInt("oc_target_hz", ocHz).apply();
                mute[0] = true;
                switchCustom.setChecked(false);
                mute[0] = false;
                prefs.edit()
                        .putBoolean("custom_app_refresh", false)
                        .putBoolean("auto_overclock", true)
                        .apply();
                String[] wh = ocRes.split("x");
                try {
                    int tw = Integer.parseInt(wh[0]), th = Integer.parseInt(wh[1]);
                    AutoOverclockManager.startService(requireContext(), prefs.getString("auth_mode", ""), tw, th, ocHz);
                    tvOCStatus.setText(R.string.guard_enabled);
                    tvOCStatus.setTextColor(0xFF2ECC71);
                    showOcTargetContainer(true);
                    startOverclockStatusUpdater();
                } catch (Exception e) {
                    mute[0] = true;
                    switchOC.setChecked(false);
                    mute[0] = false;
                    prefs.edit().putBoolean("auto_overclock", false).apply();
                }
            } else {
                AutoOverclockManager.stopService(requireContext());
                stopOverclockStatusUpdater();
                tvOCStatus.setText(R.string.guard_disabled);
                tvOCStatus.setTextColor(0xFFE74C3C);
                showOcTargetContainer(false);
            }
        });
    }
    private boolean ensureBasePermission(SwitchCompat changedSwitch) {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(getContext(), R.string.accessibility_required, Toast.LENGTH_LONG).show();
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (Exception e) { Toast.makeText(getContext(), R.string.accessibility_open_failed, Toast.LENGTH_SHORT).show(); }
            changedSwitch.setChecked(false);
            return false;
        }
        String mode = prefs.getString("auth_mode", "");
        if (mode.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_any_permission, Toast.LENGTH_SHORT).show();
            changedSwitch.setChecked(false);
            return false;
        }
        return true;
    }
    private void updateRunningTarget(String res, int hz) {
        if (!AutoOverclockManager.isRunning() || res == null || res.isEmpty() || hz <= 0) return;
        String[] wh = res.split("x");
        if (wh.length != 2) return;
        try {
            AutoOverclockManager.updateTarget(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]), hz);
        } catch (Exception ignored) {}
    }
    private void showOcTargetContainer(boolean show) {
        if (ocTargetContainer != null) ocTargetContainer.setVisibility(View.VISIBLE);
    }
    private void updateCurrentDisplayText() {
        if (tvOCCur == null || !isAdded()) return;
        float r = AutoOverclockManager.getCurrentRate(requireContext());
        String res = AutoOverclockManager.getCurrentResolution(requireContext());
        tvOCCur.setText(getString(R.string.current_rate_format, Math.round(r), res));
    }
    private void startOverclockStatusUpdater() {
        stopOverclockStatusUpdater();
        overclockHandler = new Handler(Looper.getMainLooper());
        overclockUpdater = new Runnable() {
            @Override public void run() {
                if (!isAdded()) return;
                updateCurrentDisplayText();
                if (AutoOverclockManager.isRunning()) {
                    tvOCStatus.setText(getString(R.string.guard_running_format, AutoOverclockManager.getLastLog()));
                    tvOCStatus.setTextColor(0xFF2ECC71);
                    updateUI();
                    overclockHandler.postDelayed(this, 1500);
                }
            }
        };
        overclockHandler.post(overclockUpdater);
    }
    private void stopOverclockStatusUpdater() {
        if (overclockHandler != null && overclockUpdater != null) {
            overclockHandler.removeCallbacks(overclockUpdater);
        }
    }
    private boolean isAccessibilityServiceEnabled() {
        return AccessibilityUtils.isKeepAliveServiceEnabled(getContext());
    }
    private void updateAccessibilityStatus() {
        if (tvA11yStatus == null) return;
        if (isAccessibilityServiceEnabled()) {
            tvA11yStatus.setText(R.string.accessibility_enabled);
            tvA11yStatus.setTextColor(0xFF2ECC71);
        } else {
            tvA11yStatus.setText(R.string.accessibility_disabled);
            tvA11yStatus.setTextColor(0xFFE74C3C);
        }
        updateUI();
    }
    private void updateUI() {
        if (prefs == null) return;
        float wallpaperAlpha = prefs.getFloat("wallpaper_alpha", 0.3f);
        float uiAlpha = 1f - wallpaperAlpha;
        if (titleBar != null) titleBar.setAlpha(uiAlpha);
        if (titleText != null) titleText.setAlpha(uiAlpha);
        float cardA = 1f - (prefs.getInt("custom_card_alpha", 0) / 100f);
        float textA = 1f - (prefs.getInt("custom_text_alpha", 0) / 100f);
        android.view.View root = getView();
        if (root instanceof android.view.ViewGroup) applyCustomAlpha((android.view.ViewGroup) root, cardA, textA);
        if (tvOCStatus != null) tvOCStatus.setAlpha(1f);
        if (tvOCCur != null) tvOCCur.setAlpha(1f);
        if (tvA11yStatus != null) tvA11yStatus.setAlpha(1f);
    }
    private void applyCustomAlpha(android.view.ViewGroup vg, float cardA, float textA) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            android.view.View c = vg.getChildAt(i);
            if (c instanceof androidx.cardview.widget.CardView) {
                c.setAlpha(cardA);
            } else if (c instanceof android.widget.TextView) {
                if (c != tvOCStatus && c != tvOCCur && c != tvA11yStatus) c.setAlpha(textA);
            }
            if (c instanceof android.view.ViewGroup) applyCustomAlpha((android.view.ViewGroup) c, cardA, textA);
        }
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("wallpaper_alpha".equals(key)) updateUI();
    }
    @Override
    public void onResume() {
        super.onResume();
        updateAccessibilityStatus();
        updateCurrentDisplayText();
        refreshEnabledList();
        updateUI();
    }
    private void refreshEnabledList() {
        if (llEnabledApps == null || !isAdded()) return;
        llEnabledApps.removeAllViews();
        android.content.pm.PackageManager pm = requireContext().getPackageManager();
        java.util.Map<String, ?> allPrefs = prefs.getAll();
        java.util.List<String> enabledPkgs = new java.util.ArrayList<>();
        for (String key : allPrefs.keySet()) {
            if (key.startsWith("app_refresh_enabled_")) {
                Object val = allPrefs.get(key);
                if (Boolean.TRUE.equals(val)) {
                    enabledPkgs.add(key.substring("app_refresh_enabled_".length()));
                }
            }
        }
        if (enabledPkgs.isEmpty()) {
            if (tvEnabledEmpty != null) tvEnabledEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (tvEnabledEmpty != null) tvEnabledEmpty.setVisibility(View.GONE);
        int dp12 = (int)(12 * getResources().getDisplayMetrics().density);
        int dp8  = (int)(8  * getResources().getDisplayMetrics().density);
        int dp46 = (int)(46 * getResources().getDisplayMetrics().density);
        for (String pkg : enabledPkgs) {
            String res = prefs.getString("app_refresh_res_" + pkg, "");
            int hz = prefs.getInt("app_refresh_hz_" + pkg, -1);
            android.widget.LinearLayout row = new android.widget.LinearLayout(getContext());
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp12, dp8, dp12, dp8);
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackground(android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()) >= 0
                ? requireContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground})
                    .getDrawable(0)
                : null);
            android.widget.ImageView icon = new android.widget.ImageView(getContext());
            android.widget.LinearLayout.LayoutParams iconLp =
                new android.widget.LinearLayout.LayoutParams(dp46, dp46);
            icon.setLayoutParams(iconLp);
            try {
                android.content.pm.ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                icon.setImageDrawable(pm.getApplicationIcon(ai));
            } catch (Exception e) {
                icon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
            row.addView(icon);
            android.widget.LinearLayout textBlock = new android.widget.LinearLayout(getContext());
            textBlock.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.LinearLayout.LayoutParams tbLp =
                new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tbLp.setMarginStart(dp12);
            textBlock.setLayoutParams(tbLp);
            android.widget.TextView tvName = new android.widget.TextView(getContext());
            String appLabel = pkg;
            try { appLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString(); } catch (Exception ignored) {}
            tvName.setText(appLabel);
            tvName.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16);
            tvName.setTextColor(0xFF1A1A2E);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            tvName.setMaxLines(1);
            tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
            textBlock.addView(tvName);
            android.widget.TextView tvPkg = new android.widget.TextView(getContext());
            tvPkg.setText(pkg);
            tvPkg.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
            tvPkg.setTextColor(0xFF888888);
            tvPkg.setMaxLines(1);
            tvPkg.setEllipsize(android.text.TextUtils.TruncateAt.END);
            textBlock.addView(tvPkg);
            android.widget.TextView tvRate = new android.widget.TextView(getContext());
            String rateInfo = (res.isEmpty() ? "?" : res) + (hz > 0 ? " @ " + hz + " Hz" : "");
            tvRate.setText(rateInfo);
            tvRate.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
            tvRate.setTextColor(0xFF1976D2);
            textBlock.addView(tvRate);
            row.addView(textBlock);
            final String finalPkg = pkg;
            row.setOnClickListener(v -> {
                try {
                    android.content.Intent intent = new android.content.Intent(requireContext(),
                        com.yxlanyu.refreshrate.AppRefreshConfigActivity.class);
                    intent.putExtra("pkg", finalPkg);
                    startActivity(intent);
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } catch (Exception ignored) {}
            });
            llEnabledApps.addView(row);
            if (enabledPkgs.indexOf(pkg) < enabledPkgs.size() - 1) {
                android.view.View div = new android.view.View(getContext());
                android.widget.LinearLayout.LayoutParams divLp =
                    new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divLp.setMarginStart(dp46 + dp12 + dp12);
                div.setLayoutParams(divLp);
                div.setBackgroundColor(0x1A000000);
                llEnabledApps.addView(div);
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopOverclockStatusUpdater();
        if (prefs != null) prefs.unregisterOnSharedPreferenceChangeListener(this);
        titleBar = null;
        titleText = null;
        tvOCStatus = null;
        tvOCCur = null;
        tvA11yStatus = null;
        ocTargetContainer = null;
        llEnabledApps = null;
        tvEnabledEmpty = null;
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/fragment/SettingsFragment.java << 'EOF'
package com.yxlanyu.refreshrate.fragment;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.yxlanyu.refreshrate.AppearanceActivity;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.util.RootUtils;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
import com.yxlanyu.refreshrate.util.AccessibilityUtils;
import java.util.*;
public class SettingsFragment extends Fragment {
    private SharedPreferences prefs;
    private View rootView;
    private Handler overclockHandler;
    private Runnable overclockUpdater;
    private void startOverclockStatusUpdater(TextView tvStatus, TextView tvCur) {
        overclockHandler = new Handler(Looper.getMainLooper());
        overclockUpdater = new Runnable() {
            @Override public void run() {
                if (!isAdded()) return;
                float rate = AutoOverclockManager.getCurrentRate(requireContext());
                String res  = AutoOverclockManager.getCurrentResolution(requireContext());
                tvCur.setText(getString(R.string.current_rate_format, Math.round(rate), res));
                if (AutoOverclockManager.isRunning()) {
                    tvStatus.setText(getString(R.string.guard_running_format, AutoOverclockManager.getLastLog()));
                    tvStatus.setTextColor(0xFF2ECC71);
                    overclockHandler.postDelayed(this, 1500);
                }
            }
        };
        overclockHandler.post(overclockUpdater);
    }
    private void stopOverclockStatusUpdater() {
        if (overclockHandler != null && overclockUpdater != null) {
            overclockHandler.removeCallbacks(overclockUpdater);
        }
    }
    @Override public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle s) {
        View v = inf.inflate(R.layout.fragment_settings, c, false);
        rootView = v;
        prefs = requireActivity().getSharedPreferences("s", Context.MODE_PRIVATE);
        View rowAppearance = v.findViewById(R.id.row_appearance);
        if (rowAppearance != null) {
            rowAppearance.setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), AppearanceActivity.class);
                startActivity(intent);
            });
        }
        View rowAnimEffect = v.findViewById(R.id.row_anim_effect);
        if (rowAnimEffect != null) {
            rowAnimEffect.setOnClickListener(view -> startActivity(new Intent(getActivity(), com.yxlanyu.refreshrate.AnimationActivity.class)));
        }
        View rowLanguage = v.findViewById(R.id.row_language);
        if (rowLanguage != null) {
            rowLanguage.setOnClickListener(view -> {
                startActivity(new Intent(getActivity(), com.yxlanyu.refreshrate.LanguageActivity.class));
            });
        }
        androidx.appcompat.widget.SwitchCompat switchNativeRefresh = v.findViewById(R.id.switch_native_refresh);
        if (switchNativeRefresh != null) {
            switchNativeRefresh.setChecked(prefs.getBoolean("native_refresh_overlay", false));
            switchNativeRefresh.setOnCheckedChangeListener((btn, checked) -> {
                final String mode = prefs.getString("auth_mode", "");
                final boolean useRoot = "root".equals(mode);
                final boolean useShizuku = "shizuku".equals(mode);
                if (!useRoot && !useShizuku) {
                    Toast.makeText(getContext(), R.string.rate_lock_need_auth, Toast.LENGTH_SHORT).show();
                    btn.setChecked(false);
                    return;
                }
                prefs.edit().putBoolean("native_refresh_overlay", checked).apply();
                new Thread(() -> {
                    if (useRoot) RootUtils.setNativeRefreshOverlay(checked);
                    else com.yxlanyu.refreshrate.util.ShizukuUtils.setNativeRefreshOverlay(checked);
                }).start();
            });
        }
        TextView rootStatus    = v.findViewById(R.id.tv_root_status);
        TextView shizukuStatus = v.findViewById(R.id.tv_shizuku_status);
        Button btnRequestShizuku = v.findViewById(R.id.btn_request_shizuku);
        androidx.appcompat.widget.SwitchCompat switchRoot    = v.findViewById(R.id.switch_root);
        androidx.appcompat.widget.SwitchCompat switchShizuku = v.findViewById(R.id.switch_shizuku);
        String savedMode = prefs.getString("auth_mode", "");
        switchRoot.setChecked("root".equals(savedMode));
        switchShizuku.setChecked("shizuku".equals(savedMode));
        new Thread(() -> {
            boolean ok = RootUtils.isRooted();
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    rootStatus.setText(ok ? R.string.settings_root_granted : R.string.settings_root_denied);
                    rootStatus.setTextColor(ok ? 0xFF2ECC71 : 0xFFE74C3C);
                });
            }
        }).start();
        updateShizukuStatus(shizukuStatus, btnRequestShizuku);
        btnRequestShizuku.setOnClickListener(view -> {
            if (com.yxlanyu.refreshrate.util.ShizukuUtils.isAvailable()) {
                com.yxlanyu.refreshrate.util.ShizukuUtils.requestPermission();
                new Handler(Looper.getMainLooper())
                    .postDelayed(() -> updateShizukuStatus(shizukuStatus, btnRequestShizuku), 1000);
            } else {
                Toast.makeText(getContext(), R.string.shizuku_install_hint, Toast.LENGTH_LONG).show();
            }
        });
        switchRoot.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                switchShizuku.setChecked(false);
                prefs.edit().putString("auth_mode", "root").apply();
            } else {
                if (!switchShizuku.isChecked()) {
                    prefs.edit().putString("auth_mode", "").apply();
                }
            }
        });
        switchShizuku.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                switchRoot.setChecked(false);
                prefs.edit().putString("auth_mode", "shizuku").apply();
                updateShizukuStatus(shizukuStatus, btnRequestShizuku);
            } else {
                if (!switchRoot.isChecked()) {
                    prefs.edit().putString("auth_mode", "").apply();
                }
                btnRequestShizuku.setVisibility(android.view.View.GONE);
            }
        });
        View btnAihaozhe = v.findViewById(R.id.btn_aihaozhe_link);
        if (btnAihaozhe != null) {
            btnAihaozhe.setOnClickListener(view -> {
                String url = "https://www.coolapk.com/u/31452988";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.coolapk.market"));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
        }
        View btnShagua = v.findViewById(R.id.btn_shagua_link);
        if (btnShagua != null) {
            btnShagua.setOnClickListener(view -> {
                String url = "https://www.coolapk.com/u/33802586";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.coolapk.market"));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
        }
        View btnHuaiyin = v.findViewById(R.id.btn_huaiyin_link);
        if (btnHuaiyin != null) {
            btnHuaiyin.setOnClickListener(view -> {
                String url = "https://www.coolapk.com/u/14621568";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.coolapk.market"));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
        }
        applySettingTransparency();
        Button btnGenerateLog = v.findViewById(R.id.btn_generate_log);
        if (btnGenerateLog != null) {
            btnGenerateLog.setOnClickListener(view -> {
                new Thread(() -> {
                    String log = RootUtils.generateRuntimeLog(requireContext().getApplicationContext());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> showLogDialog(log));
                    }
                }).start();
            });
        }
        androidx.core.widget.NestedScrollView settingsNsv = v.findViewById(R.id.settings_scroll_view);
        if (settingsNsv != null) {
            settingsNsv.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener)
                (nsv, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (!(getActivity() instanceof com.yxlanyu.refreshrate.MainActivity)) return;
                    com.yxlanyu.refreshrate.MainActivity ma = (com.yxlanyu.refreshrate.MainActivity) getActivity();
                    if (scrollY > oldScrollY + 8) ma.hideBottomNav();
                    else if (scrollY < oldScrollY - 8) ma.showBottomNav();
                });
        }
        return v;
    }
    @Override
    public void onResume() {
        super.onResume();
        applySettingTransparency();
        View v = getView();
        if (v != null) {
            TextView tvA11yStatus = v.findViewById(R.id.tv_accessibility_status);
            updateAccessibilityStatus(tvA11yStatus);
        }
    }
    private boolean isAccessibilityServiceEnabled() {
        return AccessibilityUtils.isKeepAliveServiceEnabled(getContext());
    }
    private void updateAccessibilityStatus(TextView tv) {
        if (tv == null) return;
        boolean enabled = isAccessibilityServiceEnabled();
        tv.setText(enabled ? R.string.accessibility_enabled : R.string.accessibility_disabled);
        tv.setTextColor(enabled ? 0xFF2ECC71 : 0xFFE74C3C);
    }
    private void updateShizukuStatus(TextView tv, Button btn) {
        boolean avail = com.yxlanyu.refreshrate.util.ShizukuUtils.isAvailable();
        boolean perm  = com.yxlanyu.refreshrate.util.ShizukuUtils.hasPermission();
        if (!avail) {
            tv.setText(R.string.shizuku_not_running);
            tv.setTextColor(0xFF888888);
            btn.setVisibility(android.view.View.GONE);
        } else if (!perm) {
            tv.setText(R.string.shizuku_no_perm);
            tv.setTextColor(0xFFE74C3C);
            btn.setVisibility(android.view.View.VISIBLE);
        } else {
            tv.setText(R.string.shizuku_authorized);
            tv.setTextColor(0xFF2ECC71);
            btn.setVisibility(android.view.View.GONE);
        }
    }
    private void applySettingTransparency() {
        if (rootView == null) return;
        int cardAlpha = 255 - (int)(prefs.getInt("setting_card_alpha", 0) / 100f * 255);
        int textAlpha = 255 - (int)(prefs.getInt("setting_text_alpha", 0) / 100f * 255);
        applyAlphaToViewGroup((ViewGroup) rootView, cardAlpha, textAlpha);
    }
    private void applyAlphaToViewGroup(ViewGroup parent, int cardAlpha, int textAlpha) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof CardView) {
                CardView cv = (CardView) child;
                cv.setCardBackgroundColor(Color.argb(cardAlpha, 255, 255, 255));
                cv.setCardElevation(0f);
                cv.setMaxCardElevation(0f);
            } else if (child instanceof TextView) {
                child.setAlpha(textAlpha / 255f);
            }
            if (child instanceof ViewGroup) {
                applyAlphaToViewGroup((ViewGroup) child, cardAlpha, textAlpha);
            }
        }
    }
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getContext(), R.string.notification_perm_denied, Toast.LENGTH_LONG).show();
            }
        }
    }
    private void showLogDialog(String logContent) {
        if (getContext() == null) return;
        java.io.File logDir = new java.io.File(requireContext().getCacheDir(), "logs");
        logDir.mkdirs();
        java.io.File logFile = new java.io.File(logDir, "refresh_rate_log.txt");
        try {
            java.io.FileWriter fw = new java.io.FileWriter(logFile);
            fw.write(logContent); fw.close();
        } catch (Exception ignored) {}
        ScrollView sv = new ScrollView(getContext());
        TextView tv = new TextView(getContext());
        tv.setText(logContent);
        tv.setPadding(30, 30, 30, 30);
        tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTextColor(Color.BLACK);
        sv.addView(tv);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.log_dialog_title)
                .setView(sv)
                .setPositiveButton(R.string.log_dialog_close, null)
                .setNeutralButton(R.string.log_dialog_share, (d, w) -> {
                    try {
                        android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().getPackageName() + ".fileprovider",
                            logFile);
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_STREAM, fileUri);
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(share, getString(R.string.log_dialog_share)));
                    } catch (Exception e) {
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_TEXT, logContent);
                        startActivity(Intent.createChooser(share, getString(R.string.log_dialog_share)));
                    }
                })
                .show();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        stopOverclockStatusUpdater();
        rootView = null;
    }
}
EOF

cat > app/src/main/java/com/yxlanyu/refreshrate/MainActivity.java << 'EOF'
package com.yxlanyu.refreshrate;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yxlanyu.refreshrate.fragment.HomeFragment;
import com.yxlanyu.refreshrate.fragment.CustomAppRefreshFragment;
import com.yxlanyu.refreshrate.fragment.SettingsFragment;
import com.yxlanyu.refreshrate.fragment.RemoteRefreshFragment;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import java.io.InputStream;
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private ImageView wallpaperView;
    private ImageView wallpaperView2;
    private boolean wp2Front = false;
    private BottomNavigationView bottomNav;
    private android.widget.LinearLayout customNav;
    private final int[] NAV_IDS = { R.id.nav_home, R.id.nav_custom_app_refresh, R.id.nav_remote, R.id.nav_settings };
    private final int[] NAV_ICONS = { R.drawable.ic_nav_home, R.drawable.ic_nav_scheme, R.drawable.ic_nav_remote, R.drawable.ic_nav_settings };
    private final int[] NAV_LABELS = { R.string.nav_home, R.string.nav_custom_app_refresh, R.string.nav_remote, R.string.nav_settings };
    private SharedPreferences prefs;
    private int currentNavId = -1;
    private boolean navHidden = false;
    private String appliedLang;
    @Override protected void onResume() {
        super.onResume();
        if (appliedLang != null && !appliedLang.equals(LanguageUtils.getCurrentLang(this))) {
            recreate();
            return;
        }
        if (wallpaperView != null && prefs != null) setWallpaperImage(false);
    }
    @Override protected void onCreate(Bundle s) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(s);
        LanguageUtils.applyLanguage(this);
        appliedLang = LanguageUtils.getCurrentLang(this);
        createNotificationChannel();
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        wallpaperView = findViewById(R.id.global_wallpaper);
        wallpaperView2 = findViewById(R.id.global_wallpaper2);
        bottomNav = findViewById(R.id.bottom_navigation);
        customNav = findViewById(R.id.custom_nav);
        buildCustomNav();
        prefs = getSharedPreferences("s", MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(this);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
            @Override public void onFragmentViewCreated(androidx.fragment.app.FragmentManager fm, androidx.fragment.app.Fragment f, android.view.View v, android.os.Bundle sb) {
                android.view.View title = v.findViewWithTag("page_title");
                if (title != null) title.setVisibility(prefs.getInt("nav_scheme", 0) == 2 ? android.view.View.GONE : android.view.View.VISIBLE);
            }
        }, false);
        applyWallpaperAndUI();
        if (s == null) {
            currentNavId = R.id.nav_home;
            loadFragment(new HomeFragment(), false);
        }
        applyNavScheme();
        bottomNav.setOnItemSelectedListener(item -> switchToNav(item.getItemId()));
        bottomNav.setOnItemReselectedListener(item -> {});
    }
    private boolean switchToNav(int id) {
        if (id == currentNavId) return true;
        showBottomNav();
        int oldNavId = currentNavId;
        Fragment fragment;
        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_custom_app_refresh) {
            fragment = new CustomAppRefreshFragment();
        } else if (id == R.id.nav_remote) {
            fragment = new RemoteRefreshFragment();
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
        } else {
            return false;
        }
        currentNavId = id;
        if (bottomNav != null && bottomNav.getMenu().findItem(id) != null) bottomNav.getMenu().findItem(id).setChecked(true);
        loadFragment(fragment, getNavIndex(id) > getNavIndex(oldNavId));
        setWallpaperImage(true);
        updateCustomNavSelection();
        return true;
    }
    private void buildCustomNav() {
        if (customNav == null) return;
        customNav.removeAllViews();
        for (int i = 0; i < NAV_IDS.length; i++) {
            final int navId = NAV_IDS[i];
            android.widget.LinearLayout item = new android.widget.LinearLayout(this);
            item.setOrientation(android.widget.LinearLayout.VERTICAL);
            item.setGravity(android.view.Gravity.CENTER);
            item.setClickable(true);
            item.setFocusable(true);
            item.setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6));
            item.setTag(navId);
            android.util.TypedValue tvv = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, tvv, true);
            item.setBackgroundResource(tvv.resourceId);
            android.widget.ImageView icon = new android.widget.ImageView(this);
            icon.setImageResource(NAV_ICONS[i]);
            icon.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dpToPx(26), dpToPx(26)));
            android.widget.TextView label = new android.widget.TextView(this);
            label.setText(getString(NAV_LABELS[i]));
            label.setTextSize(11);
            label.setMaxLines(1);
            label.setPadding(0, dpToPx(2), 0, 0);
            label.setGravity(android.view.Gravity.CENTER);
            item.addView(icon);
            item.addView(label);
            item.setOnClickListener(v -> switchToNav(navId));
            customNav.addView(item);
        }
        updateCustomNavSelection();
    }
    private void updateCustomNavSelection() {
        if (customNav == null) return;
        for (int i = 0; i < customNav.getChildCount(); i++) {
            android.view.View item = customNav.getChildAt(i);
            Object tag = item.getTag();
            boolean sel = tag != null && ((Integer) tag) == currentNavId;
            int color = sel ? 0xFF1976D2 : 0xFF9AA0A6;
            if (item instanceof android.view.ViewGroup) {
                android.view.ViewGroup vg = (android.view.ViewGroup) item;
                for (int j = 0; j < vg.getChildCount(); j++) {
                    android.view.View c = vg.getChildAt(j);
                    if (c instanceof android.widget.ImageView) ((android.widget.ImageView) c).setColorFilter(color);
                    else if (c instanceof android.widget.TextView) ((android.widget.TextView) c).setTextColor(color);
                }
            }
        }
    }
    private void applyCustomNavScheme(int scheme, android.view.View container) {
        if (customNav == null) return;
        boolean horizontal = (scheme == 2);
        customNav.setOrientation(horizontal ? android.widget.LinearLayout.HORIZONTAL : android.widget.LinearLayout.VERTICAL);
        // translucent rounded floating pill
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(android.graphics.Color.parseColor("#A6FFFFFF"));
        bg.setCornerRadius(dpToPx(28));
        bg.setStroke(dpToPx(1), android.graphics.Color.parseColor("#80FFFFFF"));
        customNav.setBackground(bg);
        customNav.setElevation(dpToPx(10));
        int pad = dpToPx(6);
        customNav.setPadding(pad, pad, pad, pad);
        int labelSp = horizontal ? 15 : 13;
        for (int i = 0; i < customNav.getChildCount(); i++) {
            android.view.View item = customNav.getChildAt(i);
            android.widget.LinearLayout.LayoutParams lp;
            if (horizontal) {
                lp = new android.widget.LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            } else {
                lp = new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dpToPx(4); lp.bottomMargin = dpToPx(4);
            }
            item.setLayoutParams(lp);
            if (item instanceof android.view.ViewGroup) {
                android.view.ViewGroup vg = (android.view.ViewGroup) item;
                for (int j = 0; j < vg.getChildCount(); j++) {
                    android.view.View c = vg.getChildAt(j);
                    if (c instanceof android.widget.ImageView) c.setVisibility(scheme == 2 ? android.view.View.GONE : android.view.View.VISIBLE);
                    else if (c instanceof android.widget.TextView) ((android.widget.TextView) c).setTextSize(labelSp);
                }
            }
        }
        android.view.ViewGroup.LayoutParams glp = customNav.getLayoutParams();
        if (glp instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams clp = (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) glp;
            int P = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            int U = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            int W = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            clp.topToTop = U; clp.bottomToBottom = U; clp.startToStart = U; clp.endToEnd = U;
            clp.leftMargin = 0; clp.rightMargin = 0; clp.topMargin = 0; clp.bottomMargin = 0;
            int m = dpToPx(12);
            if (scheme == 2) { // top floating bar
                clp.topToTop = P; clp.startToStart = P; clp.endToEnd = P;
                clp.width = 0; clp.height = W;
                clp.leftMargin = dpToPx(16); clp.rightMargin = dpToPx(16); clp.topMargin = dpToPx(10);
            } else if (scheme == 3) { // left floating, centered vertically
                clp.startToStart = P; clp.topToTop = P; clp.bottomToBottom = P;
                clp.width = dpToPx(66); clp.height = W;
                clp.leftMargin = m;
            } else { // right floating, centered vertically
                clp.endToEnd = P; clp.topToTop = P; clp.bottomToBottom = P;
                clp.width = dpToPx(66); clp.height = W;
                clp.rightMargin = m;
            }
            customNav.setLayoutParams(clp);
        }
        // side schemes push content clear of the nav so it stays tappable; top pushes content down
        if (container != null) {
            int lp = (scheme == 3) ? dpToPx(88) : 0;
            int rp = (scheme == 4) ? dpToPx(88) : 0;
            int tp = (scheme == 2) ? dpToPx(66) : 0;
            container.setPadding(lp, tp, rp, 0);
        }
        updateCustomNavSelection();
    }
    private void loadFragment(Fragment fragment, boolean forward) {
        android.content.SharedPreferences sp = getSharedPreferences("s", MODE_PRIVATE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment).commit();
        if (sp.getBoolean("anim_engine", true)) {
            final String type = sp.getString("anim_type", "spring");
            final float speed = sp.getFloat("anim_speed", 1.0f);
            final boolean fwd = forward;
            final android.view.View container = findViewById(R.id.fragment_container);
            if (container != null) container.post(() -> animatePage(container, type, speed, fwd));
        }
    }
    private void animatePage(android.view.View view, String type, float speed, boolean forward) {
        if (view == null) return;
        long dur = (long) (320 / (speed <= 0 ? 1f : speed));
        int w = view.getWidth();
        if (w <= 0) w = 400;
        view.animate().cancel();
        view.setAlpha(1f); view.setTranslationX(0f);
        if ("fade".equals(type)) {
            view.setAlpha(0f);
            view.animate().alpha(1f).setDuration(dur).setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator()).start();
        } else if ("linear".equals(type)) {
            view.setTranslationX(forward ? w : -w);
            view.animate().translationX(0f).setDuration(dur).setInterpolator(new android.view.animation.LinearInterpolator()).start();
        } else if ("slide".equals(type)) {
            view.setAlpha(0f); view.setTranslationX(forward ? w * 0.35f : -w * 0.35f);
            view.animate().translationX(0f).alpha(1f).setDuration(dur).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        } else {
            view.setTranslationX(forward ? w * 0.5f : -w * 0.5f);
            view.animate().translationX(0f).setDuration((long) (dur * 1.4f)).setInterpolator(new android.view.animation.OvershootInterpolator(1.6f)).start();
        }
    }
    private int getNavIndex(int id) {
        if (id == R.id.nav_home) return 0;
        if (id == R.id.nav_custom_app_refresh) return 1;
        if (id == R.id.nav_remote) return 2;
        if (id == R.id.nav_settings) return 3;
        return 0;
    }
    private String pageKeyForNav(int id) {
        if (id == R.id.nav_custom_app_refresh) return "app";
        if (id == R.id.nav_remote) return "remote";
        if (id == R.id.nav_settings) return "settings";
        return "home";
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "overclock_channel",
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    private void applyWallpaperAndUI() {
        float wallpaperAlpha = prefs.getFloat("wallpaper_alpha", 0.3f);
        float titleAlpha = 1f - wallpaperAlpha;
        bottomNav.getBackground().setAlpha((int)(titleAlpha * 255));
        bottomNav.setElevation(0);
        setWallpaperImage(false);
    }
    private android.graphics.drawable.Drawable currentWallpaperDrawable() {
        int type = prefs.getInt("wallpaper_type", 4);
        if (type == 3) {
            android.graphics.Bitmap bm = WallpaperStore.loadPage(this, pageKeyForNav(currentNavId));
            if (bm != null) return new android.graphics.drawable.BitmapDrawable(getResources(), bm);
            return androidx.core.content.ContextCompat.getDrawable(this, R.drawable.wallpaper_white);
        }
        return WallpaperStore.presetDrawable(this, type);
    }
    private void setWallpaperImage(boolean animate) {
        android.graphics.drawable.Drawable newD = currentWallpaperDrawable();
        if (newD == null) return;
        float a = prefs.getFloat("wallpaper_alpha", 0.3f);
        ImageView front = wp2Front ? wallpaperView2 : wallpaperView;
        ImageView back = wp2Front ? wallpaperView : wallpaperView2;
        if (back == null || front == null) { wallpaperView.setImageDrawable(newD); wallpaperView.setAlpha(a); return; }
        if (!animate) {
            front.setImageDrawable(newD);
            front.setAlpha(a);
            back.setAlpha(0f);
            return;
        }
        back.animate().cancel();
        front.animate().cancel();
        back.setImageDrawable(newD);
        back.setAlpha(0f);
        back.animate().alpha(a).setDuration(300).start();
        front.animate().alpha(0f).setDuration(300).start();
        wp2Front = !wp2Front;
    }
    private void applyNavScheme() {
        int scheme = prefs.getInt("nav_scheme", 0);
        android.view.View container = findViewById(R.id.fragment_container);
        if (scheme == 0 || scheme == 1) {
            if (customNav != null) customNav.setVisibility(android.view.View.GONE);
            if (bottomNav != null) bottomNav.setVisibility(android.view.View.VISIBLE);
            if (container != null) container.setPadding(0, 0, 0, 0);
            if (scheme == 0) applyFloatingNav(); else applyClassicNav();
        } else {
            if (bottomNav != null) bottomNav.setVisibility(android.view.View.GONE);
            if (customNav != null) customNav.setVisibility(android.view.View.VISIBLE);
            applyCustomNavScheme(scheme, container);
        }
        updatePageTitleVisibility();
    }
    private void updatePageTitleVisibility() {
        android.view.View root = findViewById(R.id.fragment_container);
        if (root == null) return;
        android.view.View title = root.findViewWithTag("page_title");
        if (title != null) title.setVisibility(prefs.getInt("nav_scheme", 0) == 2 ? android.view.View.GONE : android.view.View.VISIBLE);
    }
    private void applyFloatingNav() {
        android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) bottomNav.getLayoutParams();
        params.leftMargin = dpToPx(32);
        params.rightMargin = dpToPx(32);
        params.bottomMargin = dpToPx(24);
        bottomNav.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bottomNav.setRenderEffect(null);
        }
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(40));
        bg.setColor(android.graphics.Color.parseColor("#D8FFFFFF"));
        bg.setStroke(dpToPx(1), android.graphics.Color.parseColor("#80FFFFFF"));
        bottomNav.setBackground(bg);
        bottomNav.setElevation(dpToPx(10));
    }
    private void applyClassicNav() {
        android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) bottomNav.getLayoutParams();
        params.leftMargin = 0;
        params.rightMargin = 0;
        params.bottomMargin = 0;
        bottomNav.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bottomNav.setRenderEffect(null);
        }
        bottomNav.setBackgroundColor(android.graphics.Color.parseColor("#B0FFFFFF"));
        bottomNav.setElevation(0);
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null) return;
        if (key.startsWith("wallpaper_")) {
            applyWallpaperAndUI();
        }
        if ("nav_scheme".equals(key)) {
            applyNavScheme();
        }
    }
    public void hideBottomNav() {
        if (navHidden) return;
        navHidden = true;
        if (prefs.getInt("nav_scheme", 0) <= 1 && bottomNav != null) bottomNav.animate().translationY(bottomNav.getHeight() + dpToPx(20)).setDuration(220).start();
    }
    public void showBottomNav() {
        if (!navHidden) return;
        navHidden = false;
        if (prefs.getInt("nav_scheme", 0) <= 1 && bottomNav != null) bottomNav.animate().translationY(0).setDuration(220).start();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}
EOF

cat > app/src/main/res/layout/activity_main.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:fitsSystemWindows="true">
    <ImageView
        android:id="@+id/global_wallpaper"
        android:layout_width="0dp" android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:scaleType="centerCrop"
        android:alpha="0.3"/>
    <ImageView
        android:id="@+id/global_wallpaper2"
        android:layout_width="0dp" android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:scaleType="centerCrop"
        android:alpha="0"/>
    <FrameLayout
        android:id="@+id/fragment_container"
        android:layout_width="0dp" android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:background="@android:color/transparent"/>
    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_navigation"
        android:layout_width="0dp" android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:menu="@menu/bottom_nav_menu"
        app:labelVisibilityMode="labeled"
        android:background="#75FFFFFF"
        app:itemIconTint="#1976D2"
        app:itemTextColor="#1976D2"
        app:elevation="0dp"
        android:elevation="0dp"/>
    <LinearLayout
        android:id="@+id/custom_nav"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:visibility="gone"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:elevation="8dp"/>
</androidx.constraintlayout.widget.ConstraintLayout>
EOF

cat > app/src/main/res/layout/activity_appearance.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android" xmlns:app="http://schemas.android.com/apk/res-auto" android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical" android:fitsSystemWindows="true">
    <androidx.appcompat.widget.Toolbar android:id="@+id/toolbar_appearance" android:layout_width="match_parent" android:layout_height="?attr/actionBarSize" android:background="#F0F4FF" app:navigationIcon="@drawable/ic_back_arrow" app:title="@string/appearance_title" app:titleTextColor="#1A1A2E"/>
    <FrameLayout android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1">
        <ImageView android:id="@+id/appearance_wallpaper" android:layout_width="match_parent" android:layout_height="match_parent" android:scaleType="centerCrop" android:alpha="0.3"/>
        <ScrollView android:layout_width="match_parent" android:layout_height="match_parent" android:background="@android:color/transparent">
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_wallpaper_title" android:textSize="13sp" android:textColor="#1976D2" android:textStyle="bold" android:paddingBottom="6dp" android:paddingTop="8dp"/>
            <androidx.cardview.widget.CardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardCornerRadius="12dp" app:cardBackgroundColor="#FFFFFF" app:cardElevation="0dp">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                    <TextView android:id="@+id/wp_custom" android:layout_width="match_parent" android:layout_height="46dp" android:gravity="center_vertical" android:paddingStart="12dp" android:paddingEnd="12dp" android:text="@string/wallpaper_custom" android:textSize="14sp" android:textStyle="bold" android:drawablePadding="6dp" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A" android:layout_marginBottom="6dp"/>
                    <LinearLayout android:id="@+id/ll_custom_bg" android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:layout_marginBottom="8dp">
                        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal">
                            <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:layout_margin="3dp" android:padding="8dp" android:background="@drawable/bg_tab_unselected">
                                <TextView android:layout_width="match_parent" android:layout_height="wrap_content" android:text="@string/nav_home" android:textSize="12sp" android:textStyle="bold" android:maxLines="1" android:ellipsize="end" android:textColor="@color/text_dark"/>
                                <ImageView android:id="@+id/iv_bg_home" android:layout_width="match_parent" android:layout_height="120dp" android:scaleType="fitCenter" android:adjustViewBounds="true" android:layout_marginTop="6dp" android:visibility="gone"/>
                                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">
                                    <TextView android:id="@+id/btn_clear_home" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:gravity="center" android:text="@string/bg_clear" android:textSize="13sp" android:textColor="#888888" android:padding="4dp" android:visibility="gone"/>
                                    <TextView android:id="@+id/btn_pick_home" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:gravity="center" android:text="@string/bg_select" android:textSize="13sp" android:textStyle="bold" android:textColor="#1976D2" android:padding="4dp"/>
                                </LinearLayout>
                            </LinearLayout>
                            <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:layout_margin="3dp" android:padding="8dp" android:background="@drawable/bg_tab_unselected">
                                <TextView android:layout_width="match_parent" android:layout_height="wrap_content" android:text="@string/nav_custom_app_refresh" android:textSize="12sp" android:textStyle="bold" android:maxLines="1" android:ellipsize="end" android:textColor="@color/text_dark"/>
                                <ImageView android:id="@+id/iv_bg_app" android:layout_width="match_parent" android:layout_height="120dp" android:scaleType="fitCenter" android:adjustViewBounds="true" android:layout_marginTop="6dp" android:visibility="gone"/>
                                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">
                                    <TextView android:id="@+id/btn_clear_app" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:gravity="center" android:text="@string/bg_clear" android:textSize="13sp" android:textColor="#888888" android:padding="4dp" android:visibility="gone"/>
                                    <TextView android:id="@+id/btn_pick_app" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:gravity="center" android:text="@string/bg_select" android:textSize="13sp" android:textStyle="bold" android:textColor="#1976D2" android:padding="4dp"/>
                                </LinearLayout>
                            </LinearLayout>
                        </LinearLayout>
                        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="2dp">
                            <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:layout_margin="3dp" android:padding="8dp" android:background="@drawable/bg_tab_unselected">
                                <TextView android:layout_width="match_parent" android:layout_height="wrap_content" android:text="@string/nav_remote" android:textSize="12sp" android:textStyle="bold" android:maxLines="1" android:ellipsize="end" android:textColor="@color/text_dark"/>
                                <ImageView android:id="@+id/iv_bg_remote" android:layout_width="match_parent" android:layout_height="120dp" android:scaleType="fitCenter" android:adjustViewBounds="true" android:layout_marginTop="6dp" android:visibility="gone"/>
                                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">
                                    <TextView android:id="@+id/btn_clear_remote" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:gravity="center" android:text="@string/bg_clear" android:textSize="13sp" android:textColor="#888888" android:padding="4dp" android:visibility="gone"/>
                                    <TextView android:id="@+id/btn_pick_remote" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:gravity="center" android:text="@string/bg_select" android:textSize="13sp" android:textStyle="bold" android:textColor="#1976D2" android:padding="4dp"/>
                                </LinearLayout>
                            </LinearLayout>
                            <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:layout_margin="3dp" android:padding="8dp" android:background="@drawable/bg_tab_unselected">
                                <TextView android:layout_width="match_parent" android:layout_height="wrap_content" android:text="@string/nav_settings" android:textSize="12sp" android:textStyle="bold" android:maxLines="1" android:ellipsize="end" android:textColor="@color/text_dark"/>
                                <ImageView android:id="@+id/iv_bg_settings" android:layout_width="match_parent" android:layout_height="120dp" android:scaleType="fitCenter" android:adjustViewBounds="true" android:layout_marginTop="6dp" android:visibility="gone"/>
                                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">
                                    <TextView android:id="@+id/btn_clear_settings" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:gravity="center" android:text="@string/bg_clear" android:textSize="13sp" android:textColor="#888888" android:padding="4dp" android:visibility="gone"/>
                                    <TextView android:id="@+id/btn_pick_settings" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:gravity="center" android:text="@string/bg_select" android:textSize="13sp" android:textStyle="bold" android:textColor="#1976D2" android:padding="4dp"/>
                                </LinearLayout>
                            </LinearLayout>
                        </LinearLayout>
                    </LinearLayout>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal">
                        <TextView android:id="@+id/wp_white" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_margin="3dp" android:gravity="center" android:text="@string/wallpaper_white" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A"/>
                        <TextView android:id="@+id/wp_black" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_margin="3dp" android:gravity="center" android:text="@string/wallpaper_black" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A"/>
                        <TextView android:id="@+id/wp_sunset" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_margin="3dp" android:gravity="center" android:text="@string/wallpaper_gradient1" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A"/>
                    </LinearLayout>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="2dp">
                        <TextView android:id="@+id/wp_aurora" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_margin="3dp" android:gravity="center" android:text="@string/wallpaper_gradient2" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A"/>
                        <TextView android:id="@+id/wp_emerald" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_margin="3dp" android:gravity="center" android:text="@string/wallpaper_gradient3" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A"/>
                        <TextView android:id="@+id/wp_sky" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_margin="3dp" android:gravity="center" android:text="@string/wp_sky" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A"/>
                    </LinearLayout>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="2dp">
                        <TextView android:id="@+id/wp_pink" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_margin="3dp" android:gravity="center" android:text="@string/wp_pink" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A"/>
                        <TextView android:id="@+id/wp_mint" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_margin="3dp" android:gravity="center" android:text="@string/wp_mint" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A"/>
                        <TextView android:id="@+id/wp_purple" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_margin="3dp" android:gravity="center" android:text="@string/wp_purple" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" android:background="@drawable/bg_tab_unselected" android:textColor="#546E7A"/>
                    </LinearLayout>
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/wallpaper_alpha_label" android:layout_marginTop="12dp" android:textColor="#1A1A2E"/>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginTop="6dp">
                        <SeekBar android:id="@+id/sb_alpha" android:layout_width="0dp" android:layout_weight="1" android:layout_height="46dp" android:max="100" android:progress="30" android:progressDrawable="@drawable/seekbar_track_bright" android:thumb="@null" android:background="@null" android:splitTrack="false" android:paddingStart="2dp" android:paddingEnd="2dp"/>
                        <TextView android:id="@+id/tv_alpha_pct" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="30%" android:textSize="14sp" android:textColor="#1976D2" android:textStyle="bold" android:minWidth="44dp" android:gravity="end" android:layout_marginStart="12dp"/>
                    </LinearLayout>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/nav_scheme_title" android:textSize="13sp" android:textColor="#1976D2" android:textStyle="bold" android:paddingBottom="6dp" android:paddingTop="8dp"/>
            <androidx.cardview.widget.CardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardCornerRadius="12dp" app:cardBackgroundColor="#FFFFFF" app:cardElevation="0dp">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical">
                    <LinearLayout android:id="@+id/row_nav_style" android:layout_width="match_parent" android:layout_height="56dp" android:orientation="horizontal" android:gravity="center_vertical" android:paddingStart="16dp" android:paddingEnd="16dp" android:background="?attr/selectableItemBackground" android:clickable="true" android:focusable="true">
                        <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:text="@string/nav_scheme_title" android:textSize="15sp" android:textColor="#1A1A2E"/>
                        <TextView android:id="@+id/tv_nav_style_current" android:layout_width="wrap_content" android:layout_height="wrap_content" android:textSize="13sp" android:textColor="#888888" android:layout_marginEnd="8dp"/>
                        <ImageView android:layout_width="20dp" android:layout_height="20dp" android:src="@drawable/ic_nav_settings" android:alpha="0.4"/>
                    </LinearLayout>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/card_display_title" android:textSize="13sp" android:textColor="#1976D2" android:textStyle="bold" android:paddingBottom="6dp" android:paddingTop="8dp"/>
            <androidx.cardview.widget.CardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardCornerRadius="12dp" app:cardBackgroundColor="#FFFFFF" app:cardElevation="0dp">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/card_alpha_label" android:textColor="#1A1A2E"/>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginTop="4dp">
                        <SeekBar android:id="@+id/sb_card_alpha" android:layout_width="0dp" android:layout_weight="1" android:layout_height="46dp" android:max="100" android:progress="0" android:progressDrawable="@drawable/seekbar_track_bright" android:thumb="@null" android:background="@null" android:splitTrack="false" android:paddingStart="2dp" android:paddingEnd="2dp"/>
                        <TextView android:id="@+id/tv_pct_card" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="0%" android:textSize="14sp" android:textColor="#1976D2" android:textStyle="bold" android:minWidth="44dp" android:gravity="end" android:layout_marginStart="12dp"/>
                    </LinearLayout>
                    </LinearLayout>
            </androidx.cardview.widget.CardView>
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_page_beautify" android:textSize="13sp" android:textColor="#1976D2" android:textStyle="bold" android:paddingBottom="6dp" android:paddingTop="8dp"/>
            <androidx.cardview.widget.CardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardCornerRadius="12dp" app:cardBackgroundColor="#FFFFFF" app:cardElevation="0dp">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_card_alpha" android:textColor="#1A1A2E"/>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginTop="4dp">
                        <SeekBar android:id="@+id/sb_setting_card_alpha" android:layout_width="0dp" android:layout_weight="1" android:layout_height="46dp" android:max="100" android:progress="0" android:progressDrawable="@drawable/seekbar_track_bright" android:thumb="@null" android:background="@null" android:splitTrack="false" android:paddingStart="2dp" android:paddingEnd="2dp"/>
                        <TextView android:id="@+id/tv_pct_setting_card" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="0%" android:textSize="14sp" android:textColor="#1976D2" android:textStyle="bold" android:minWidth="44dp" android:gravity="end" android:layout_marginStart="12dp"/>
                    </LinearLayout>
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_text_alpha" android:layout_marginTop="12dp" android:textColor="#1A1A2E"/>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginTop="4dp">
                        <SeekBar android:id="@+id/sb_setting_text_alpha" android:layout_width="0dp" android:layout_weight="1" android:layout_height="46dp" android:max="100" android:progress="0" android:progressDrawable="@drawable/seekbar_track_bright" android:thumb="@null" android:background="@null" android:splitTrack="false" android:paddingStart="2dp" android:paddingEnd="2dp"/>
                        <TextView android:id="@+id/tv_pct_setting_text" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="0%" android:textSize="14sp" android:textColor="#1976D2" android:textStyle="bold" android:minWidth="44dp" android:gravity="end" android:layout_marginStart="12dp"/>
                    </LinearLayout>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/app_page_alpha_title" android:textSize="13sp" android:textColor="#1976D2" android:textStyle="bold" android:paddingBottom="6dp" android:paddingTop="8dp"/>
            <androidx.cardview.widget.CardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardCornerRadius="12dp" app:cardBackgroundColor="#FFFFFF" app:cardElevation="0dp">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_card_alpha" android:textColor="#1A1A2E"/>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginTop="4dp">
                        <SeekBar android:id="@+id/sb_custom_card_alpha" android:layout_width="0dp" android:layout_weight="1" android:layout_height="46dp" android:max="100" android:progress="0" android:progressDrawable="@drawable/seekbar_track_bright" android:thumb="@null" android:background="@null" android:splitTrack="false" android:paddingStart="2dp" android:paddingEnd="2dp"/>
                        <TextView android:id="@+id/tv_pct_custom_card" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="0%" android:textSize="14sp" android:textColor="#1976D2" android:textStyle="bold" android:minWidth="44dp" android:gravity="end" android:layout_marginStart="12dp"/>
                    </LinearLayout>
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_text_alpha" android:layout_marginTop="12dp" android:textColor="#1A1A2E"/>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginTop="4dp">
                        <SeekBar android:id="@+id/sb_custom_text_alpha" android:layout_width="0dp" android:layout_weight="1" android:layout_height="46dp" android:max="100" android:progress="0" android:progressDrawable="@drawable/seekbar_track_bright" android:thumb="@null" android:background="@null" android:splitTrack="false" android:paddingStart="2dp" android:paddingEnd="2dp"/>
                        <TextView android:id="@+id/tv_pct_custom_text" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="0%" android:textSize="14sp" android:textColor="#1976D2" android:textStyle="bold" android:minWidth="44dp" android:gravity="end" android:layout_marginStart="12dp"/>
                    </LinearLayout>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/remote_page_alpha_title" android:textSize="13sp" android:textColor="#1976D2" android:textStyle="bold" android:paddingBottom="6dp" android:paddingTop="8dp"/>
            <androidx.cardview.widget.CardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardCornerRadius="12dp" app:cardBackgroundColor="#FFFFFF" app:cardElevation="0dp">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_card_alpha" android:textColor="#1A1A2E"/>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginTop="4dp">
                        <SeekBar android:id="@+id/sb_remote_card_alpha" android:layout_width="0dp" android:layout_weight="1" android:layout_height="46dp" android:max="100" android:progress="0" android:progressDrawable="@drawable/seekbar_track_bright" android:thumb="@null" android:background="@null" android:splitTrack="false" android:paddingStart="2dp" android:paddingEnd="2dp"/>
                        <TextView android:id="@+id/tv_pct_remote_card" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="0%" android:textSize="14sp" android:textColor="#1976D2" android:textStyle="bold" android:minWidth="44dp" android:gravity="end" android:layout_marginStart="12dp"/>
                    </LinearLayout>
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_text_alpha" android:layout_marginTop="12dp" android:textColor="#1A1A2E"/>
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginTop="4dp">
                        <SeekBar android:id="@+id/sb_remote_text_alpha" android:layout_width="0dp" android:layout_weight="1" android:layout_height="46dp" android:max="100" android:progress="0" android:progressDrawable="@drawable/seekbar_track_bright" android:thumb="@null" android:background="@null" android:splitTrack="false" android:paddingStart="2dp" android:paddingEnd="2dp"/>
                        <TextView android:id="@+id/tv_pct_remote_text" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="0%" android:textSize="14sp" android:textColor="#1976D2" android:textStyle="bold" android:minWidth="44dp" android:gravity="end" android:layout_marginStart="12dp"/>
                    </LinearLayout>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
        </LinearLayout>
    </ScrollView>
    </FrameLayout>
</LinearLayout>
EOF


cat > app/src/main/res/layout/fragment_home.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/transparent"
    android:fitsSystemWindows="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:background="@android:color/transparent">
        <RelativeLayout
            android:id="@+id/home_title_bar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="20dp"
            android:background="#55FFFFFF">
            <TextView
                android:id="@+id/home_title_text"
                android:tag="page_title"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/home_title"
                android:textSize="26sp"
                android:textStyle="bold"
                android:textColor="@color/text_dark"
                android:layout_centerVertical="true"/>
        </RelativeLayout>
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recycler_rates"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:padding="16dp"
            android:background="@android:color/transparent"/>
    </LinearLayout>
</FrameLayout>
EOF

          cat > app/src/main/res/layout/fragment_custom_app_refresh.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/transparent"
    android:fitsSystemWindows="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:background="@android:color/transparent">
        <RelativeLayout
            android:id="@+id/custom_app_refresh_title_bar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="20dp"
            android:background="#55FFFFFF">
            <TextView
                android:id="@+id/custom_app_refresh_title_text"
                android:tag="page_title"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/nav_custom_app_refresh"
                android:textSize="26sp"
                android:textStyle="bold"
                android:textColor="@color/text_dark"
                android:layout_centerVertical="true"/>
        </RelativeLayout>
        <androidx.core.widget.NestedScrollView
            android:id="@+id/custom_app_refresh_scroll"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:padding="16dp"
            android:clipToPadding="false"
            android:background="@android:color/transparent">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:paddingBottom="24dp">
                <androidx.cardview.widget.CardView
                    android:id="@+id/card_app_management"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="14dp"
                    app:cardCornerRadius="14dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="#AAFFFFFF">
                    <LinearLayout
                        android:id="@+id/row_app_management"
                        android:layout_width="match_parent"
                        android:layout_height="72dp"
                        android:orientation="horizontal"
                        android:gravity="center_vertical"
                        android:paddingStart="16dp"
                        android:paddingEnd="12dp"
                        android:clickable="true"
                        android:focusable="true"
                        android:background="?attr/selectableItemBackground">
                        <ImageView
                            android:layout_width="28dp"
                            android:layout_height="28dp"
                            android:src="@drawable/ic_apps_grid"
                            android:contentDescription="@string/app_management_title"/>
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:layout_marginStart="18dp"
                            android:text="@string/app_management_title"
                            android:textSize="17sp"
                            android:textColor="@color/text_dark"/>
                        <ImageView
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:src="@drawable/ic_chevron_right"/>
                    </LinearLayout>
                </androidx.cardview.widget.CardView>
                <androidx.cardview.widget.CardView
                    android:id="@+id/card_enabled_list"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="14dp"
                    app:cardCornerRadius="14dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="#AAFFFFFF">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="@string/enabled_app_list_title"
                            android:textSize="16sp"
                            android:textStyle="bold"
                            android:textColor="@color/text_dark"
                            android:layout_marginBottom="8dp"/>
                        <LinearLayout
                            android:id="@+id/ll_enabled_apps"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"/>
                        <TextView
                            android:id="@+id/tv_enabled_empty"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:gravity="center"
                            android:padding="12dp"
                            android:text="@string/enabled_list_empty"
                            android:textColor="@color/text_muted"
                            android:visibility="gone"/>
                    </LinearLayout>
                </androidx.cardview.widget.CardView>
                <androidx.cardview.widget.CardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="14dp"
                    app:cardCornerRadius="14dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="#AAFFFFFF">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:gravity="center_vertical">
                            <LinearLayout
                                android:layout_width="0dp"
                                android:layout_height="wrap_content"
                                android:layout_weight="1"
                                android:orientation="vertical">
                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="@string/nav_custom_app_refresh"
                                    android:textSize="16sp"
                                    android:textStyle="bold"
                                    android:textColor="@color/text_dark"/>
                                <TextView
                                    android:layout_width="match_parent"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="4dp"
                                    android:text="@string/custom_app_refresh_desc"
                                    android:textSize="12sp"
                                    android:textColor="@color/text_muted"/>
                            </LinearLayout>
                            <androidx.appcompat.widget.SwitchCompat
                                app:track="@drawable/switch_track_md3"
                                android:switchMinWidth="33dp"
                                android:thumbTextPadding="0dp"
                                app:thumbTint="@null"
                                app:trackTint="@null"
                                android:thumb="@drawable/switch_thumb_md3"
                                android:id="@+id/switch_custom_app_refresh"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"/>
                        </LinearLayout>
                    </LinearLayout>
                </androidx.cardview.widget.CardView>
                <androidx.cardview.widget.CardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="14dp"
                    app:cardCornerRadius="14dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="#AAFFFFFF">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:gravity="center_vertical">
                            <LinearLayout
                                android:layout_width="0dp"
                                android:layout_height="wrap_content"
                                android:layout_weight="1"
                                android:orientation="vertical">
                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="@string/auto_overclock_title"
                                    android:textSize="16sp"
                                    android:textStyle="bold"
                                    android:textColor="@color/text_dark"/>
                                <TextView
                                    android:layout_width="match_parent"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="4dp"
                                    android:text="@string/auto_overclock_desc"
                                    android:textSize="12sp"
                                    android:textColor="@color/text_muted"/>
                                <TextView
                                    android:id="@+id/tv_overclock_cur"
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="4dp"
                                    android:text="@string/current_rate_format"
                                    android:textSize="12sp"
                                    android:textColor="@color/text_muted"/>
                            </LinearLayout>
                            <androidx.appcompat.widget.SwitchCompat
                                app:track="@drawable/switch_track_md3"
                                android:switchMinWidth="33dp"
                                android:thumbTextPadding="0dp"
                                app:thumbTint="@null"
                                app:trackTint="@null"
                                android:thumb="@drawable/switch_thumb_md3"
                                android:id="@+id/switch_auto_overclock"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"/>
                        </LinearLayout>
                        <TextView
                            android:id="@+id/tv_overclock_status"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="@string/guard_disabled"
                            android:textSize="13sp"
                            android:layout_marginTop="8dp"
                            android:textColor="#E74C3C"/>
                        <LinearLayout
                            android:id="@+id/ll_oc_target_container"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:layout_marginTop="14dp"
                            android:visibility="visible">
                            <LinearLayout
                                android:layout_width="0dp"
                                android:layout_height="wrap_content"
                                android:layout_weight="1"
                                android:orientation="vertical"
                                android:layout_marginEnd="8dp">
                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="@string/target_resolution_label"
                                    android:textSize="13sp"
                                    android:textColor="@color/text_muted"/>
                                <Spinner
                                    android:id="@+id/spinner_oc_resolution"
                                    android:layout_width="match_parent"
                                    android:layout_height="48dp"/>
                            </LinearLayout>
                            <LinearLayout
                                android:layout_width="0dp"
                                android:layout_height="wrap_content"
                                android:layout_weight="1"
                                android:orientation="vertical"
                                android:layout_marginStart="8dp">
                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="@string/target_rate_label"
                                    android:textSize="13sp"
                                    android:textColor="@color/text_muted"/>
                                <Spinner
                                    android:id="@+id/spinner_oc_rate"
                                    android:layout_width="match_parent"
                                    android:layout_height="48dp"/>
                            </LinearLayout>
                        </LinearLayout>
                    </LinearLayout>
                </androidx.cardview.widget.CardView>
                <androidx.cardview.widget.CardView android:layout_width="match_parent"
                    android:layout_height="wrap_content" android:layout_marginTop="4dp" android:layout_marginBottom="4dp"
                    app:cardCornerRadius="16dp" app:cardBackgroundColor="#AAFFFFFF"
                    app:cardElevation="0dp">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
                        android:orientation="vertical" android:padding="16dp">
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="@string/perm_status_title"
                            android:textSize="16sp"
                            android:textStyle="bold"
                            android:textColor="@color/text_dark"/>
                        <TextView
                            android:id="@+id/tv_accessibility_status"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="8dp"
                            android:text="@string/accessibility_disabled"
                            android:textSize="13sp"
                            android:textColor="#E74C3C"/>
                    </LinearLayout>
                </androidx.cardview.widget.CardView>
            </LinearLayout>
        </androidx.core.widget.NestedScrollView>
    </LinearLayout>
</FrameLayout>
EOF

cat > app/src/main/res/layout/item_rate_card.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:padding="16dp" android:orientation="vertical">
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/card_rate"
        android:layout_width="match_parent" android:layout_height="wrap_content"
        app:cardCornerRadius="20dp"
        app:strokeWidth="1dp"
        app:strokeColor="#90FFFFFF"
        app:cardElevation="0dp">
        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
            <RelativeLayout android:layout_width="match_parent" android:layout_height="wrap_content"
                android:padding="18dp">
                <TextView android:id="@+id/tv_rate_name" android:layout_width="wrap_content"
                    android:layout_height="wrap_content" android:textColor="#FFFFFF"
                    android:textSize="13sp" android:alpha="0.85"/>
                <TextView android:id="@+id/tv_rate_hz" android:layout_width="wrap_content"
                    android:layout_height="wrap_content" android:textSize="34sp"
                    android:textStyle="bold" android:textColor="#FFFFFF"
                    android:layout_below="@id/tv_rate_name"/>
                <TextView android:id="@+id/tv_rate_desc" android:layout_width="wrap_content"
                    android:layout_height="wrap_content" android:textColor="#DDFFFFFF"
                    android:textSize="11sp"
                    android:layout_below="@id/tv_rate_hz" android:layout_marginTop="4dp"/>
                <TextView android:id="@+id/tv_rate_debug" android:layout_width="wrap_content"
                    android:layout_height="wrap_content" android:textColor="#FFFFFFFF"
                    android:textSize="10sp" android:alpha="0.92" android:visibility="gone"
                    android:layout_alignParentBottom="true" android:layout_alignParentRight="true"/>
            </RelativeLayout>
            <View
                android:layout_width="match_parent"
                android:layout_height="90dp"
                android:background="@drawable/glass_highlight"
                android:layout_gravity="top"/>
        </FrameLayout>
    </com.google.android.material.card.MaterialCardView>
</LinearLayout>
EOF

cat > app/src/main/res/layout/item_res_header.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:padding="16dp"
    android:background="@android:color/transparent">
    <TextView android:id="@+id/tv_header_title"
        android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:textStyle="bold" android:textColor="@color/text_dark"/>
</LinearLayout>
EOF

cat > app/src/main/res/layout/fragment_settings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android" xmlns:app="http://schemas.android.com/apk/res-auto" android:id="@+id/settings_scroll_view" android:layout_width="match_parent" android:layout_height="match_parent" android:background="@android:color/transparent" android:fitsSystemWindows="true" android:paddingBottom="72dp" android:clipToPadding="false">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_title" android:tag="page_title" android:textSize="23sp" android:textStyle="bold" android:textColor="@color/text_dark" android:layout_marginStart="4dp" android:layout_marginTop="8dp" android:layout_marginBottom="20dp"/>
        <androidx.cardview.widget.CardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardCornerRadius="12dp" app:cardBackgroundColor="#AAFFFFFF" app:cardElevation="0dp">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical">
                <LinearLayout android:id="@+id/row_appearance" android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal" android:gravity="center_vertical" android:paddingStart="16dp" android:paddingEnd="16dp" android:clickable="true" android:focusable="true" android:background="?attr/selectableItemBackground">
                    <ImageView android:layout_width="32dp" android:layout_height="32dp" android:src="@drawable/ic_appearance" android:layout_marginEnd="14dp"/>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/appearance_title" android:textSize="16sp" android:textStyle="bold" android:textColor="@color/text_dark"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/appearance_desc" android:textSize="12sp" android:textColor="@color/text_muted"/>
                    </LinearLayout>
                    <ImageView android:layout_width="20dp" android:layout_height="20dp" android:src="@drawable/ic_chevron_right" android:alpha="0.5"/>
                </LinearLayout>
                <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#15000000" android:layout_marginStart="16dp" android:layout_marginEnd="16dp"/>
                <LinearLayout android:id="@+id/row_anim_effect" android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal" android:gravity="center_vertical" android:paddingStart="16dp" android:paddingEnd="16dp" android:clickable="true" android:focusable="true" android:background="?attr/selectableItemBackground">
                    <ImageView android:layout_width="32dp" android:layout_height="32dp" android:src="@drawable/ic_sun" android:layout_marginEnd="14dp"/>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/anim_effect_title" android:textSize="16sp" android:textStyle="bold" android:textColor="@color/text_dark"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/anim_engine_desc" android:textSize="12sp" android:textColor="@color/text_muted"/>
                    </LinearLayout>
                    <ImageView android:layout_width="20dp" android:layout_height="20dp" android:src="@drawable/ic_chevron_right" android:alpha="0.5"/>
                </LinearLayout>
                <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#15000000" android:layout_marginStart="16dp" android:layout_marginEnd="16dp"/>
                <LinearLayout android:id="@+id/row_language" android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal" android:gravity="center_vertical" android:paddingStart="16dp" android:paddingEnd="16dp" android:clickable="true" android:focusable="true" android:background="?attr/selectableItemBackground">
                    <ImageView android:layout_width="32dp" android:layout_height="32dp" android:src="@drawable/ic_translate" android:layout_marginEnd="14dp"/>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/language_page_title" android:textSize="16sp" android:textStyle="bold" android:textColor="@color/text_dark"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/language_row_desc" android:textSize="12sp" android:textColor="@color/text_muted"/>
                    </LinearLayout>
                    <ImageView android:layout_width="20dp" android:layout_height="20dp" android:src="@drawable/ic_chevron_right" android:alpha="0.5"/>
                </LinearLayout>
                <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#15000000" android:layout_marginStart="16dp" android:layout_marginEnd="16dp"/>
                <LinearLayout android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal" android:gravity="center_vertical" android:paddingStart="16dp" android:paddingEnd="16dp">
                    <ImageView android:layout_width="32dp" android:layout_height="32dp" android:src="@drawable/ic_android_head" android:layout_marginEnd="14dp"/>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/show_refresh_rate_label" android:textSize="16sp" android:textStyle="bold" android:textColor="@color/text_dark"/>
                    </LinearLayout>
                    <androidx.appcompat.widget.SwitchCompat app:track="@drawable/switch_track_md3" android:switchMinWidth="33dp" android:thumbTextPadding="0dp" app:thumbTint="@null" app:trackTint="@null" android:thumb="@drawable/switch_thumb_md3" android:id="@+id/switch_native_refresh" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
                </LinearLayout>
            </LinearLayout>
        </androidx.cardview.widget.CardView>
        <androidx.cardview.widget.CardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardCornerRadius="12dp" app:cardBackgroundColor="#AAFFFFFF" app:cardElevation="0dp">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/settings_root_title" android:textStyle="bold" android:textSize="18sp" android:textColor="@color/text_dark" android:paddingBottom="12dp"/>
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical">
                    <ImageView android:layout_width="28dp" android:layout_height="28dp" android:src="@drawable/ic_magisk" android:layout_marginEnd="10dp"/>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/root_running_label" android:textSize="15sp" android:textColor="@color/text_dark"/>
                        <TextView android:id="@+id/tv_root_status" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="检测中..." android:textSize="12sp" android:textColor="@color/text_muted"/>
                    </LinearLayout>
                    <androidx.appcompat.widget.SwitchCompat app:track="@drawable/switch_track_md3" android:switchMinWidth="33dp" android:thumbTextPadding="0dp" app:thumbTint="@null" app:trackTint="@null" android:thumb="@drawable/switch_thumb_md3" android:id="@+id/switch_root" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
                </LinearLayout>
                <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#22000000" android:layout_marginTop="14dp" android:layout_marginBottom="14dp"/>
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical">
                    <ImageView android:layout_width="28dp" android:layout_height="28dp" android:src="@drawable/ic_shizuku" android:layout_marginEnd="10dp"/>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/shizuku_running_label" android:textSize="15sp" android:textColor="@color/text_dark"/>
                        <TextView android:id="@+id/tv_shizuku_status" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="检测中..." android:textSize="12sp" android:textColor="@color/text_muted"/>
                    </LinearLayout>
                    <androidx.appcompat.widget.SwitchCompat app:track="@drawable/switch_track_md3" android:switchMinWidth="33dp" android:thumbTextPadding="0dp" app:thumbTint="@null" app:trackTint="@null" android:thumb="@drawable/switch_thumb_md3" android:id="@+id/switch_shizuku" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
                </LinearLayout>
                <Button android:id="@+id/btn_request_shizuku" android:layout_width="match_parent" android:layout_height="48dp" android:text="@string/btn_authorize_shizuku" android:layout_marginTop="10dp" android:visibility="gone" style="@style/Widget.MaterialComponents.Button.OutlinedButton"/>
            </LinearLayout>
        </androidx.cardview.widget.CardView>
        <androidx.cardview.widget.CardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="14dp" app:cardCornerRadius="14dp" app:cardElevation="0dp" app:cardBackgroundColor="#AAFFFFFF">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/about_title" android:textStyle="bold" android:textSize="18sp" android:textColor="@color/text_dark"/>
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/version_label" android:layout_marginTop="4dp" android:textColor="@color/text_dark"/>
                <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#22000000" android:layout_marginTop="14dp" android:layout_marginBottom="12dp"/>
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/contributors_title" android:textStyle="bold" android:textSize="15sp" android:textColor="@color/text_dark" android:layout_marginBottom="10dp"/>
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical">
                    <androidx.cardview.widget.CardView android:layout_width="48dp" android:layout_height="48dp" app:cardCornerRadius="24dp" app:cardElevation="0dp">
                        <ImageView android:layout_width="match_parent" android:layout_height="match_parent" android:scaleType="centerCrop" android:src="@drawable/avatar_aihaozhe"/>
                    </androidx.cardview.widget.CardView>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:layout_marginStart="12dp">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/contrib_type_creator" android:textSize="14sp" android:textStyle="bold" android:textColor="@color/text_dark"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/contrib_info_aihaozhe" android:textSize="12sp" android:layout_marginTop="2dp" android:textColor="@color/text_muted"/>
                    </LinearLayout>
                    <ImageView android:id="@+id/btn_aihaozhe_link" android:layout_width="40dp" android:layout_height="40dp" android:padding="8dp" android:src="@drawable/ic_send_plane" android:clickable="true" android:focusable="true" android:background="?attr/selectableItemBackgroundBorderless" android:contentDescription="@string/contrib_jump"/>
                </LinearLayout>
                <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#15000000" android:layout_marginTop="12dp" android:layout_marginBottom="12dp"/>
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical">
                    <androidx.cardview.widget.CardView android:layout_width="48dp" android:layout_height="48dp" app:cardCornerRadius="24dp" app:cardElevation="0dp">
                        <ImageView android:layout_width="match_parent" android:layout_height="match_parent" android:scaleType="centerCrop" android:src="@drawable/avatar_shagua"/>
                    </androidx.cardview.widget.CardView>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:layout_marginStart="12dp">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/contrib_type_coder" android:textSize="14sp" android:textStyle="bold" android:textColor="@color/text_dark"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/contrib_info_shagua" android:textSize="12sp" android:layout_marginTop="2dp" android:textColor="@color/text_muted"/>
                    </LinearLayout>
                    <ImageView android:id="@+id/btn_shagua_link" android:layout_width="40dp" android:layout_height="40dp" android:padding="8dp" android:src="@drawable/ic_send_plane" android:clickable="true" android:focusable="true" android:background="?attr/selectableItemBackgroundBorderless" android:contentDescription="@string/contrib_jump"/>
                </LinearLayout>
                <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#15000000" android:layout_marginTop="12dp" android:layout_marginBottom="12dp"/>
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical">
                    <androidx.cardview.widget.CardView android:layout_width="48dp" android:layout_height="48dp" app:cardCornerRadius="24dp" app:cardElevation="0dp">
                        <ImageView android:layout_width="match_parent" android:layout_height="match_parent" android:scaleType="centerCrop" android:src="@drawable/avatar_huaiyin"/>
                    </androidx.cardview.widget.CardView>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:layout_marginStart="12dp">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/contrib_type_supporter" android:textSize="14sp" android:textStyle="bold" android:textColor="@color/text_dark"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/contrib_info_huaiyin" android:textSize="12sp" android:layout_marginTop="2dp" android:textColor="@color/text_muted"/>
                    </LinearLayout>
                    <ImageView android:id="@+id/btn_huaiyin_link" android:layout_width="40dp" android:layout_height="40dp" android:padding="8dp" android:src="@drawable/ic_send_plane" android:clickable="true" android:focusable="true" android:background="?attr/selectableItemBackgroundBorderless" android:contentDescription="@string/contrib_jump"/>
                </LinearLayout>
            </LinearLayout>
        </androidx.cardview.widget.CardView>
        <Button android:id="@+id/btn_generate_log" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="@string/generate_log_btn" style="@style/Widget.MaterialComponents.Button"/>
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
EOF

cat > app/src/main/res/menu/bottom_nav_menu.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/nav_home" android:icon="@drawable/ic_nav_home" android:title="@string/nav_home"/>
    <item android:id="@+id/nav_custom_app_refresh" android:icon="@drawable/ic_nav_scheme" android:title="@string/nav_custom_app_refresh"/>
    <item android:id="@+id/nav_remote" android:icon="@drawable/ic_nav_remote" android:title="@string/nav_remote"/>
    <item android:id="@+id/nav_settings" android:icon="@drawable/ic_nav_settings" android:title="@string/nav_settings"/>
</menu>
EOF

cat > app/src/main/res/values/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">屏幕刷新率</string>
    <string name="home_title">屏幕刷新率</string>
    <string name="appearance_title">外观</string>
    <string name="appearance_desc">壁纸美化、档位透明度、设置页透明度</string>
    <string name="nav_scheme_title">导航栏样式</string>
    <string name="nav_scheme_float">悬浮导航</string>
    <string name="nav_scheme_classic">经典导航</string>
    <string name="nav_scheme_top">顶部导航栏</string>
    <string name="nav_scheme_left">左侧导航栏</string>
    <string name="nav_scheme_right">右侧导航栏</string>
    <string name="high_resolution">高分辨率  </string>
    <string name="low_resolution">低分辨率  </string>
    <string name="fallback_high_res">高分辨率</string>
    <string name="fallback_low_res">低分辨率</string>
    <string name="rate_name_supreme">鎏金超限</string>
    <string name="rate_name_limit">极限</string>
    <string name="rate_name_high">高刷</string>
    <string name="rate_name_extreme">极致</string>
    <string name="rate_name_excellent">极佳</string>
    <string name="rate_name_smooth">流畅</string>
    <string name="rate_name_standard">标准</string>
    <string name="rate_desc_supreme">金芒破限，视界抵达巅峰</string>
    <string name="rate_desc_limit">巅峰画面质感，诠释极限视觉体验</string>
    <string name="rate_desc_high">画面瞬时响应，无拖影卡顿</string>
    <string name="rate_desc_extreme">极致的画面体验，画面快人一步</string>
    <string name="rate_desc_excellent">高等刷新率，拥有超流畅的画面体验</string>
    <string name="rate_desc_smooth">高的刷新率，画面流畅</string>
    <string name="rate_desc_standard">常规刷新率，延长续航</string>
    <string name="no_root_toast">请授权Root/Shizuku权限</string>
    <string name="switch_success">已切换至 %d Hz</string>
    <string name="switch_fail">切换失败，请检查 Root 授权</string>
    <string name="settings_root_title">授权状态</string>
    <string name="settings_root_granted">已获取 Root 权限</string>
    <string name="settings_root_denied">未获取 Root 权限</string>
    <string name="settings_wallpaper_title">背景</string>
    <string name="wallpaper_gradient1">日落渐变</string>
    <string name="wp_sky">天蓝</string>
    <string name="wp_pink">樱粉</string>
    <string name="wp_mint">薄荷</string>
    <string name="wp_purple">暮紫</string>
    <string name="wallpaper_gradient2">极光渐变</string>
    <string name="wallpaper_gradient3">翡翠渐变</string>
    <string name="wallpaper_white">纯白</string>
    <string name="wallpaper_black">纯黑</string>
    <string name="wallpaper_custom">自定义背景</string>
    <string name="bg_clear">清除</string>
    <string name="bg_select">选择</string>
    <string name="wallpaper_alpha_label">壁纸透明度</string>
    <string name="card_display_title">档位透明度</string>
    <string name="card_alpha_label">档位透明度</string>
    <string name="settings_page_beautify">设置页透明度</string>
    <string name="settings_card_alpha">卡片透明度</string>
    <string name="settings_text_alpha">文字透明度</string>
    <string name="app_page_alpha_title">自定义应用刷新率页</string>
    <string name="remote_page_alpha_title">远程刷新率页</string>
    <string name="root_running_label">Root 运行</string>
    <string name="shizuku_running_label">Shizuku 运行</string>
    <string name="about_title">关于</string>
    <string name="version_label">版本: 1.6.5</string>
    <string name="generate_log_btn">生成日志</string>
    <string name="language_zh">简体中文</string>
    <string name="language_zh_tw">繁體中文</string>
    <string name="language_en">English</string>
    <string name="language_ja">日本語</string>
    <string name="nav_home">主页</string>
    <string name="nav_custom_app_refresh">自定义应用刷新率</string>
    <string name="nav_remote">远程刷新率</string>
    <string name="remote_mode_wireless">无线连接</string>
    <string name="remote_mode_usb">USB连接</string>
    <string name="remote_ip_hint">设备 IP 地址</string>
    <string name="remote_port_hint">连接端口</string>
    <string name="remote_pair_hint">配对码</string>
    <string name="remote_connect_btn">连接</string>
    <string name="remote_status_idle">未连接</string>
    <string name="remote_status_none">未连接设备</string>
    <string name="remote_modes_title">被控端刷新率</string>
    <string name="remote_hint">在被控手机开启「无线调试」，用「使用配对码配对设备」获取 IP、端口和配对码。</string>
    <string name="remote_usb_hint">用数据线连接被控设备，并在被控端授权 USB 调试。</string>
    <string name="remote_status_need_input">请输入 IP 地址和端口</string>
    <string name="remote_pair_port_hint">配对端口</string>
    <string name="remote_pair_btn">配对设备</string>
    <string name="remote_section_pair">无线调试配对</string>
    <string name="remote_blur_ip">隐藏 IP 地址（点击输入栏查看）</string>
    <string name="remote_max_devices">最多同时连接 3 台设备</string>
    <string name="remote_section_connect">连接</string>
    <string name="remote_need_pair_input">请填写 IP、配对端口和配对码</string>
    <string name="remote_pairing">正在配对…</string>
    <string name="remote_pair_ok">配对成功，请填连接端口后点连接</string>
    <string name="remote_pair_fail">配对失败：%1$s</string>
    <string name="remote_conn_wait">正在连接…</string>
    <string name="remote_connected">已连接 %1$s</string>
    <string name="remote_wireless_device">无线调试设备</string>
    <string name="debug_mode_title">调试模式</string>
    <string name="anim_engine_title">动画引擎</string>
    <string name="anim_effect_title">动画效果</string>
    <string name="anim_engine_desc">页面切换时使用丝滑动画</string>
    <string name="anim_type_title">动画类型</string>
    <string name="anim_speed_title">动画速度</string>
    <string name="anim_type_spring">物理弹簧</string>
    <string name="anim_type_linear">线性运动</string>
    <string name="anim_type_fade">淡入淡出</string>
    <string name="anim_type_slide">滑动</string>
    <string name="remote_conn_fail">连接失败：%1$s</string>
    <string name="remote_switching">正在切换到 %1$d Hz…</string>
    <string name="remote_switch_ok">已切换到 %1$d Hz</string>
    <string name="remote_switch_fail">切换失败：%1$s</string>
    <string name="remote_modes_parse_fail">无法读取刷新率，请确认设备已连接</string>
    <string name="remote_history_title">历史记录</string>
    <string name="remote_switch_sent">命令已发送（无法读取当前刷新率）</string>
    <string name="remote_switch_actual">已发送，当前 %1$d Hz（该档位可能被锁定）</string>
    <string name="nav_settings">设置</string>
    <string name="log_dialog_title">运行日志</string>
    <string name="log_dialog_close">关闭</string>
    <string name="log_dialog_share">分享</string>
    <string name="custom_wallpaper_saved">自定义壁纸已保存</string>
    <string name="need_storage_permission">需要存储权限才能选择图片</string>
    <string name="permission_go_settings">去设置</string>
    <string name="permission_cancel">取消</string>
    <string name="permission_denied_title">需要存储权限</string>
    <string name="permission_denied_message">您已拒绝权限，请前往系统设置手动授予，否则无法选择图片。</string>
    <string name="accessibility_enabled">无障碍已开启</string>
    <string name="accessibility_disabled">无障碍未开启（点击开关将引导开启）</string>
    <string name="shizuku_install_hint">请先安装并启动 Shizuku</string>
    <string name="auto_overclock_title">屏幕刷新率自动超频</string>
    <string name="guard_enabled">自动启动守护已开启</string>
    <string name="guard_disabled">自动启动守护未开启</string>
    <string name="target_resolution_label">目标分辨率</string>
    <string name="target_rate_label">目标刷新率</string>
    <string name="shizuku_not_running">Shizuku 未运行（需先安装启动）</string>
    <string name="shizuku_no_perm">Shizuku 运行中，未授权</string>
    <string name="shizuku_authorized">Shizuku 已授权</string>
    <string name="btn_authorize_shizuku">授权 Shizuku</string>
    <string name="accessibility_required">请先开启无障碍服务以确保守护常驻</string>
    <string name="accessibility_open_failed">无法打开无障碍设置</string>
    <string name="notification_perm_denied">通知权限被拒绝，守护通知可能无法显示</string>
    <string name="notification_channel_name">自动超频守护</string>
    <string name="notification_title">自动超频守护运行中</string>
    <string name="notification_stop">停止</string>
    <string name="current_rate_format" formatted="false">当前刷新率: %d Hz  分辨率: %s</string>
    <string name="guard_running_format">自动启动守护已开启  %s</string>
    <string name="guard_watching_format">守护中 @ %d Hz</string>
    <string name="guard_start_format" formatted="false">守护启动，目标：%s @ %d Hz</string>
    <string name="guard_stopped">守护已停止</string>
    <string name="guard_no_res_format">未找到分辨率 %s</string>
    <string name="guard_target_updated" formatted="false">目标已更新 → %s @ %d Hz</string>
    <string name="guard_res_fix">分辨率校正，</string>
    <string name="guard_hz_up" formatted="false">%s%d Hz ↑ → %d Hz</string>
    <string name="guard_hz_down" formatted="false">%d Hz ↓ → %d Hz</string>
    <string name="show_refresh_rate_label">显示/隐藏刷新率</string>
    <string name="enabled_app_list_title">启用名单</string>
    <string name="enabled_list_empty">暂无已启用的应用</string>
    <string name="app_management_title">应用管理</string>
    <string name="custom_app_refresh_desc">用于应用管理里面的自定义应用刷新率。打开目标应用时，软件会自动切到你在应用管理里给该应用选择的分辨率和刷新率。</string>
    <string name="auto_overclock_desc">用于全局选择自定义刷新率与分辨率。当系统或应用强制降低分辨率或刷新率时，软件将帮你拉回原本设定的刷新率与分辨率。</string>
    <string name="app_refresh_config_title">应用配置</string>
    <string name="enable_single_app_refresh">启用单独应用刷新率</string>
    <string name="single_app_refresh_desc">开启后，进入这个应用时会自动切换到下面选择的分辨率和刷新率。</string>
    <string name="search_app_hint">搜索应用</string>
    <string name="show_system_apps">显示系统应用</string>
    <string name="app_list_third_party_hint">默认仅显示第三方应用，点右上角三个点可显示系统应用。</string>
    <string name="app_list_all_hint">当前正在显示第三方应用和系统应用。</string>
    <string name="no_apps_found">没有找到应用</string>
    <string name="app_list_load_failed">读取应用列表失败，请检查访问应用列表权限</string>
    <string name="app_list_permission_title">访问应用列表权限</string>
    <string name="app_list_permission_message">应用管理需要读取已安装应用列表，才能显示软件名称和软件包名。如果系统限制了此权限，请在系统设置中允许本应用访问应用列表。</string>
    <string name="no_any_permission">你没有赋予任何权限，请先在设置里选择 Root 或 Shizuku 授权。</string>
    <string name="custom_app_master_required">请先返回开启自定义应用刷新率总开关</string>
    <string name="rate_lock_need_auth">请先在设置里授予 Root 或 Shizuku 权限</string>
    <string name="custom_notif_channel_name">指定应用刷新率</string>
    <string name="custom_notif_stop">停止</string>
    <string name="custom_notif_status_on">[该应用已启用自定义刷新率]</string>
    <string name="custom_notif_status_off">[该应用未启用自定义刷新率]</string>
    <string name="custom_notif_set_format">已设置  分辨率: %1$s  刷新率: %2$d Hz</string>
    <string name="custom_notif_current_format">当前分辨率: %1$s  当前刷新率: %2$d Hz</string>
    <string name="contributors_title">关于我们</string>
    <string name="contrib_type_creator">贡献类型: 创作者</string>
    <string name="contrib_info_aihaozhe">个人信息: 酷安 爱好者ahh</string>
    <string name="contrib_jump">前往关注</string>
    <string name="contrib_type_coder">贡献类型: 代码贡献者&amp;支持者</string>
    <string name="contrib_info_shagua">个人信息: 酷安 傻瓜我爱你呀喵</string>
    <string name="contrib_type_supporter">贡献类型: 支持者</string>
    <string name="contrib_info_huaiyin">个人信息: 酷安 槐蔭區</string>
    <string name="custom_notif_waiting_title">自定义应用刷新率 · 运行中</string>
    <string name="custom_notif_waiting_text">等待切换到已启用的应用…</string>
    <string name="perm_status_title">权限授权状态</string>
    <string name="language_page_title">语言设置</string>
    <string name="lang_system">系统默认</string>
    <string name="lang_zh">简体中文</string>
    <string name="lang_zh_tw">繁體中文</string>
    <string name="lang_en">English</string>
    <string name="lang_ja">日本語</string>
    <string name="home_card_alpha_label">主页卡片透明度</string>
    <string name="custom_card_alpha_label">自定义页卡片透明度</string>
    <string name="custom_text_alpha_label">自定义页文字透明度</string>
    <string name="settings_title">设置</string>
    <string name="language_row_desc">选择应用显示语言</string>
    <string name="check_update">检查更新</string>
    <string name="update_available_title">有可用更新</string>
    <string name="update_available_msg">检测到新版本 %1$s</string>
    <string name="update_btn">更新</string>
    <string name="update_close">关闭</string>
    <string name="update_latest_toast">当前已是最新版本</string>
    <string name="update_checking_toast">正在检查更新</string>
    <string name="update_check_fail">检查更新失败，请稍后重试</string>
    <string name="update_changelog_label">更新内容：</string>
    <string name="update_downloading">正在下载更新</string>
    <string name="update_allow_install">请先允许安装未知应用权限，然后重新点击更新</string>
</resources>
EOF

cat > app/src/main/res/values-en/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Refresh Rate</string>
    <string name="home_title">Refresh Rate</string>
    <string name="appearance_title">Appearance</string>
    <string name="appearance_desc">Wallpaper, gear transparency, settings transparency</string>
    <string name="nav_scheme_title">Navigation Style</string>
    <string name="nav_scheme_float">Float Navigation</string>
    <string name="nav_scheme_classic">Classic Navigation</string>
    <string name="nav_scheme_top">Top bar</string>
    <string name="nav_scheme_left">Left bar</string>
    <string name="nav_scheme_right">Right bar</string>
    <string name="high_resolution">High Resolution  </string>
    <string name="low_resolution">Low Resolution  </string>
    <string name="fallback_high_res">High Resolution</string>
    <string name="fallback_low_res">Low Resolution</string>
    <string name="rate_name_supreme">Supreme</string>
    <string name="rate_name_limit">Limit</string>
    <string name="rate_name_high">High</string>
    <string name="rate_name_extreme">Extreme</string>
    <string name="rate_name_excellent">Excellent</string>
    <string name="rate_name_smooth">Smooth</string>
    <string name="rate_name_standard">Standard</string>
    <string name="rate_desc_supreme">Beyond limits, ultimate visual peak</string>
    <string name="rate_desc_limit">Peak visuals, ultimate experience</string>
    <string name="rate_desc_high">Instant response, no ghosting</string>
    <string name="rate_desc_extreme">Ultimate visuals, one step ahead</string>
    <string name="rate_desc_excellent">High refresh, ultra smooth</string>
    <string name="rate_desc_smooth">High refresh, smooth display</string>
    <string name="rate_desc_standard">Standard refresh, long battery life</string>
    <string name="no_root_toast">Please authorize Root/Shizuku</string>
    <string name="switch_success">Switched to %d Hz</string>
    <string name="switch_fail">Switch failed, check root access</string>
    <string name="settings_root_title">Authorization Status</string>
    <string name="settings_root_granted">Root access granted</string>
    <string name="settings_root_denied">No root access</string>
    <string name="settings_wallpaper_title">Background</string>
    <string name="wallpaper_gradient1">Sunset</string>
    <string name="wp_sky">Sky</string>
    <string name="wp_pink">Pink</string>
    <string name="wp_mint">Mint</string>
    <string name="wp_purple">Purple</string>
    <string name="wallpaper_gradient2">Aurora</string>
    <string name="wallpaper_gradient3">Emerald</string>
    <string name="wallpaper_white">Pure White</string>
    <string name="wallpaper_black">Pure Black</string>
    <string name="wallpaper_custom">Custom Background</string>
    <string name="bg_clear">Clear</string>
    <string name="bg_select">Select</string>
    <string name="wallpaper_alpha_label">Wallpaper Alpha</string>
    <string name="card_display_title">Gear Transparency</string>
    <string name="card_alpha_label">Card Alpha</string>
    <string name="settings_page_beautify">Settings Transparency</string>
    <string name="settings_card_alpha">Card Alpha</string>
    <string name="settings_text_alpha">Text Alpha</string>
    <string name="app_page_alpha_title">Custom app refresh page</string>
    <string name="remote_page_alpha_title">Remote refresh page</string>
    <string name="root_running_label">Root Running</string>
    <string name="shizuku_running_label">Shizuku Running</string>
    <string name="about_title">About</string>
    <string name="version_label">Version: 1.6.5</string>
    <string name="generate_log_btn">Generate Log</string>
    <string name="language_zh">简体中文</string>
    <string name="language_zh_tw">繁體中文</string>
    <string name="language_en">English</string>
    <string name="language_ja">日本語</string>
    <string name="nav_home">Home</string>
    <string name="nav_custom_app_refresh">Custom App Refresh Rate</string>
    <string name="nav_remote">Remote</string>
    <string name="remote_mode_wireless">Wireless</string>
    <string name="remote_mode_usb">USB</string>
    <string name="remote_ip_hint">Device IP</string>
    <string name="remote_port_hint">Connect port</string>
    <string name="remote_pair_hint">Pairing code</string>
    <string name="remote_connect_btn">Connect</string>
    <string name="remote_status_idle">Not connected</string>
    <string name="remote_status_none">No device connected</string>
    <string name="remote_modes_title">Remote refresh rates</string>
    <string name="remote_hint">On the controlled phone, enable Wireless debugging and use Pair device with pairing code to get the IP, port and code.</string>
    <string name="remote_usb_hint">Connect the controlled device via USB cable and authorize USB debugging on it.</string>
    <string name="remote_status_need_input">Please enter IP address and port</string>
    <string name="remote_pair_port_hint">Pairing port</string>
    <string name="remote_pair_btn">Pair device</string>
    <string name="remote_section_pair">Wireless debug pairing</string>
    <string name="remote_blur_ip">Hide IP (tap the input box to view)</string>
    <string name="remote_max_devices">Up to 3 devices at once</string>
    <string name="remote_section_connect">Connect</string>
    <string name="remote_need_pair_input">Enter IP, pairing port and code</string>
    <string name="remote_pairing">Pairing…</string>
    <string name="remote_pair_ok">Paired. Enter connect port, then tap Connect.</string>
    <string name="remote_pair_fail">Pair failed: %1$s</string>
    <string name="remote_conn_wait">Connecting…</string>
    <string name="remote_connected">Connected to %1$s</string>
    <string name="remote_wireless_device">wireless debug device</string>
    <string name="debug_mode_title">Debug Mode</string>
    <string name="anim_engine_title">Animation Engine</string>
    <string name="anim_effect_title">Animation Effects</string>
    <string name="anim_engine_desc">Smooth animation when switching pages</string>
    <string name="anim_type_title">Animation Type</string>
    <string name="anim_speed_title">Animation Speed</string>
    <string name="anim_type_spring">Physics Spring</string>
    <string name="anim_type_linear">Linear</string>
    <string name="anim_type_fade">Fade</string>
    <string name="anim_type_slide">Slide</string>
    <string name="remote_conn_fail">Connect failed: %1$s</string>
    <string name="remote_switching">Switching to %1$d Hz…</string>
    <string name="remote_switch_ok">Switched to %1$d Hz</string>
    <string name="remote_switch_fail">Switch failed: %1$s</string>
    <string name="remote_modes_parse_fail">Could not read refresh rates. Make sure the device is connected.</string>
    <string name="remote_history_title">History</string>
    <string name="remote_switch_sent">Sent (could not read current refresh rate)</string>
    <string name="remote_switch_actual">Sent, now at %1$d Hz (rate may be locked)</string>
    <string name="nav_settings">Settings</string>
    <string name="log_dialog_title">Runtime Log</string>
    <string name="log_dialog_close">Close</string>
    <string name="log_dialog_share">Share</string>
    <string name="custom_wallpaper_saved">Custom wallpaper saved</string>
    <string name="need_storage_permission">Storage permission required</string>
    <string name="permission_go_settings">Go to Settings</string>
    <string name="permission_cancel">Cancel</string>
    <string name="permission_denied_title">Storage Permission</string>
    <string name="permission_denied_message">You have denied the permission. Please grant it manually in system settings.</string>
    <string name="accessibility_enabled">Accessibility enabled</string>
    <string name="accessibility_disabled">Accessibility not enabled (tap switch to enable)</string>
    <string name="shizuku_install_hint">Please install and start Shizuku first</string>
    <string name="auto_overclock_title">Auto Refresh Rate</string>
    <string name="guard_enabled">Auto guard enabled</string>
    <string name="guard_disabled">Auto guard not enabled</string>
    <string name="target_resolution_label">Target Resolution</string>
    <string name="target_rate_label">Target Rate</string>
    <string name="shizuku_not_running">Shizuku not running (install first)</string>
    <string name="shizuku_no_perm">Shizuku running, not authorized</string>
    <string name="shizuku_authorized">Shizuku authorized</string>
    <string name="btn_authorize_shizuku">Authorize Shizuku</string>
    <string name="accessibility_required">Enable accessibility service to keep guard alive</string>
    <string name="accessibility_open_failed">Cannot open accessibility settings</string>
    <string name="notification_perm_denied">Notification permission denied, guard notification may not show</string>
    <string name="notification_channel_name">Auto Overclock Guard</string>
    <string name="notification_title">Auto overclock guard running</string>
    <string name="notification_stop">Stop</string>
    <string name="current_rate_format" formatted="false">Rate: %d Hz  Res: %s</string>
    <string name="guard_running_format">Guard enabled  %s</string>
    <string name="guard_watching_format">Guarding @ %d Hz</string>
    <string name="guard_start_format" formatted="false">Guard started, target: %s @ %d Hz</string>
    <string name="guard_stopped">Guard stopped</string>
    <string name="guard_no_res_format">Resolution not found: %s</string>
    <string name="guard_target_updated" formatted="false">Target updated → %s @ %d Hz</string>
    <string name="guard_res_fix">Resolution fix, </string>
    <string name="guard_hz_up" formatted="false">%s%d Hz ↑ → %d Hz</string>
    <string name="guard_hz_down" formatted="false">%d Hz ↓ → %d Hz</string>
    <string name="show_refresh_rate_label">Show/Hide Refresh Rate</string>
    <string name="enabled_app_list_title">Enabled List</string>
    <string name="enabled_list_empty">No apps enabled yet</string>
    <string name="app_management_title">App management</string>
    <string name="custom_app_refresh_desc">Used for custom refresh rate in App management. When you open a target app, this app switches to the resolution and refresh rate you selected for that app.</string>
    <string name="auto_overclock_desc">Used to choose a global custom refresh rate and resolution. When the system or an app forces them lower, this app pulls them back to your original settings.</string>
    <string name="app_refresh_config_title">App config</string>
    <string name="enable_single_app_refresh">Enable per-app refresh rate</string>
    <string name="single_app_refresh_desc">After enabling, entering this app switches to the resolution and refresh rate selected below.</string>
    <string name="search_app_hint">Search apps</string>
    <string name="show_system_apps">Show system apps</string>
    <string name="app_list_third_party_hint">Third-party apps are shown by default. Tap the three-dot menu to show system apps.</string>
    <string name="app_list_all_hint">Showing both third-party apps and system apps.</string>
    <string name="no_apps_found">No apps found</string>
    <string name="app_list_load_failed">Failed to read app list. Check app list access permission.</string>
    <string name="app_list_permission_title">App list access</string>
    <string name="app_list_permission_message">App management needs to read installed apps to show app names and package names. If your system restricts it, allow this app to access the app list in system settings.</string>
    <string name="no_any_permission">No permission has been granted. Please choose Root or Shizuku authorization in Settings first.</string>
    <string name="custom_app_master_required">Please go back and turn on the Custom App Refresh Rate master switch first</string>
    <string name="rate_lock_need_auth">Grant Root or Shizuku permission first</string>
    <string name="custom_notif_channel_name">Per-App Refresh Rate</string>
    <string name="custom_notif_stop">Stop</string>
    <string name="custom_notif_status_on">[Custom refresh rate ENABLED]</string>
    <string name="custom_notif_status_off">[Custom refresh rate NOT enabled]</string>
    <string name="custom_notif_set_format">Set  Resolution: %1$s  Rate: %2$d Hz</string>
    <string name="custom_notif_current_format">Current resolution: %1$s  Current rate: %2$d Hz</string>
    <string name="contributors_title">About Us</string>
    <string name="contrib_type_creator">Type: Creator</string>
    <string name="contrib_info_aihaozhe">Profile: Coolapk 爱好者ahh</string>
    <string name="contrib_jump">Open profile</string>
    <string name="contrib_type_coder">Type: Code Contributor &amp; Supporter</string>
    <string name="contrib_info_shagua">Profile: Coolapk 傻瓜我爱你呀喵</string>
    <string name="contrib_type_supporter">Type: Supporter</string>
    <string name="contrib_info_huaiyin">Profile: Coolapk 槐蔭區</string>
    <string name="custom_notif_waiting_title">Per-app refresh rate · Running</string>
    <string name="custom_notif_waiting_text">Waiting to switch to an enabled app…</string>
    <string name="perm_status_title">Permission status</string>
    <string name="language_page_title">Language</string>
    <string name="lang_system">系统默认</string>
    <string name="lang_zh">简体中文</string>
    <string name="lang_zh_tw">繁體中文</string>
    <string name="lang_en">English</string>
    <string name="lang_ja">日本語</string>
    <string name="home_card_alpha_label">Home card transparency</string>
    <string name="custom_card_alpha_label">Custom page card transparency</string>
    <string name="custom_text_alpha_label">Custom page text transparency</string>
    <string name="settings_title">Settings</string>
    <string name="language_row_desc">Choose app display language</string>
    <string name="check_update">Check for updates</string>
    <string name="update_available_title">Update available</string>
    <string name="update_available_msg">New version %1$s is available</string>
    <string name="update_btn">Update</string>
    <string name="update_close">Close</string>
    <string name="update_latest_toast">You are on the latest version</string>
    <string name="update_checking_toast">Checking for updates</string>
    <string name="update_check_fail">Update check failed, try again later</string>
    <string name="update_changelog_label">Changelog:</string>
    <string name="update_downloading">Downloading update</string>
    <string name="update_allow_install">Please allow installing unknown apps, then tap Update again</string>
</resources>
EOF

cat > app/src/main/res/values-ja/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">リフレッシュレート</string>
    <string name="home_title">リフレッシュレート</string>
    <string name="appearance_title">外観</string>
    <string name="appearance_desc">壁紙・段階の透明度・設定の透明度</string>
    <string name="nav_scheme_title">ナビゲーションスタイル</string>
    <string name="nav_scheme_float">フローティングナビ</string>
    <string name="nav_scheme_classic">クラシックナビ</string>
    <string name="nav_scheme_top">上部バー</string>
    <string name="nav_scheme_left">左側バー</string>
    <string name="nav_scheme_right">右側バー</string>
    <string name="high_resolution">高解像度  </string>
    <string name="low_resolution">低解像度  </string>
    <string name="fallback_high_res">高解像度</string>
    <string name="fallback_low_res">低解像度</string>
    <string name="rate_name_supreme">至高</string>
    <string name="rate_name_limit">限界</string>
    <string name="rate_name_high">高</string>
    <string name="rate_name_extreme">極致</string>
    <string name="rate_name_excellent">極佳</string>
    <string name="rate_name_smooth">滑らか</string>
    <string name="rate_name_standard">標準</string>
    <string name="rate_desc_supreme">限界突破、究極の視覚体験</string>
    <string name="rate_desc_limit">究極の画面、限界視覚体験</string>
    <string name="rate_desc_high">瞬時応答、残像なし</string>
    <string name="rate_desc_extreme">究極の画面体験、一歩先へ</string>
    <string name="rate_desc_excellent">高リフレッシュレート、超滑らか</string>
    <string name="rate_desc_smooth">高リフレッシュレート、滑らかな表示</string>
    <string name="rate_desc_standard">通常のリフレッシュレート、長持ちバッテリー</string>
    <string name="no_root_toast">Root/Shizuku権限を付与してください</string>
    <string name="switch_success">%d Hz に切り替えました</string>
    <string name="switch_fail">切り替えに失敗しました。Root権限を確認してください</string>
    <string name="settings_root_title">認証状態</string>
    <string name="settings_root_granted">Root権限を取得しました</string>
    <string name="settings_root_denied">Root権限がありません</string>
    <string name="settings_wallpaper_title">背景</string>
    <string name="wallpaper_gradient1">サンセット</string>
    <string name="wp_sky">スカイ</string>
    <string name="wp_pink">ピンク</string>
    <string name="wp_mint">ミント</string>
    <string name="wp_purple">パープル</string>
    <string name="wallpaper_gradient2">オーロラ</string>
    <string name="wallpaper_gradient3">エメラルド</string>
    <string name="wallpaper_white">純白</string>
    <string name="wallpaper_black">純黒</string>
    <string name="wallpaper_custom">カスタム背景</string>
    <string name="bg_clear">クリア</string>
    <string name="bg_select">選択</string>
    <string name="wallpaper_alpha_label">壁紙の透明度</string>
    <string name="card_display_title">段階の透明度</string>
    <string name="card_alpha_label">カードの透明度</string>
    <string name="settings_page_beautify">設定の透明度</string>
    <string name="settings_card_alpha">カードの透明度</string>
    <string name="settings_text_alpha">テキストの透明度</string>
    <string name="app_page_alpha_title">カスタムアプリページ</string>
    <string name="remote_page_alpha_title">リモートページ</string>
    <string name="root_running_label">Root 実行</string>
    <string name="shizuku_running_label">Shizuku 実行</string>
    <string name="about_title">について</string>
    <string name="version_label">バージョン: 1.6.5</string>
    <string name="generate_log_btn">ログ生成</string>
    <string name="language_zh">简体中文</string>
    <string name="language_zh_tw">繁體中文</string>
    <string name="language_en">English</string>
    <string name="language_ja">日本語</string>
    <string name="nav_home">ホーム</string>
    <string name="nav_custom_app_refresh">アプリ別リフレッシュレート</string>
    <string name="nav_remote">リモート</string>
    <string name="remote_mode_wireless">無線接続</string>
    <string name="remote_mode_usb">USB接続</string>
    <string name="remote_ip_hint">デバイスIP</string>
    <string name="remote_port_hint">接続ポート</string>
    <string name="remote_pair_hint">ペアリングコード</string>
    <string name="remote_connect_btn">接続</string>
    <string name="remote_status_idle">未接続</string>
    <string name="remote_status_none">デバイス未接続</string>
    <string name="remote_modes_title">リモートのリフレッシュレート</string>
    <string name="remote_hint">制御される端末で「ワイヤレスデバッグ」を有効にし、「ペア設定コードでデバイスをペア設定」からIP・ポート・コードを取得します。</string>
    <string name="remote_usb_hint">USBケーブルで制御される端末を接続し、USBデバッグを許可してください。</string>
    <string name="remote_status_need_input">IPアドレスとポートを入力してください</string>
    <string name="remote_pair_port_hint">ペアリングポート</string>
    <string name="remote_pair_btn">デバイスをペア設定</string>
    <string name="remote_section_pair">ワイヤレスデバッグのペア設定</string>
    <string name="remote_blur_ip">IPを隠す（入力欄をタップで表示）</string>
    <string name="remote_max_devices">同時接続は最大3台</string>
    <string name="remote_section_connect">接続</string>
    <string name="remote_need_pair_input">IP・ペアリングポート・コードを入力してください</string>
    <string name="remote_pairing">ペアリング中…</string>
    <string name="remote_pair_ok">ペア設定完了。接続ポートを入力して接続してください</string>
    <string name="remote_pair_fail">ペアリング失敗：%1$s</string>
    <string name="remote_conn_wait">接続中…</string>
    <string name="remote_connected">%1$s に接続しました</string>
    <string name="remote_wireless_device">ワイヤレスデバッグ端末</string>
    <string name="debug_mode_title">デバッグモード</string>
    <string name="anim_engine_title">アニメーションエンジン</string>
    <string name="anim_effect_title">アニメーション効果</string>
    <string name="anim_engine_desc">ページ切替時に滑らかなアニメーション</string>
    <string name="anim_type_title">アニメーションの種類</string>
    <string name="anim_speed_title">アニメーション速度</string>
    <string name="anim_type_spring">物理スプリング</string>
    <string name="anim_type_linear">リニア</string>
    <string name="anim_type_fade">フェード</string>
    <string name="anim_type_slide">スライド</string>
    <string name="remote_conn_fail">接続失敗：%1$s</string>
    <string name="remote_switching">%1$d Hz に切り替え中…</string>
    <string name="remote_switch_ok">%1$d Hz に切り替えました</string>
    <string name="remote_switch_fail">切り替え失敗：%1$s</string>
    <string name="remote_modes_parse_fail">リフレッシュレートを取得できません。接続を確認してください</string>
    <string name="remote_history_title">履歴</string>
    <string name="remote_switch_sent">送信しました（現在のリフレッシュレートを取得できません）</string>
    <string name="remote_switch_actual">送信、現在 %1$d Hz（ロックの可能性）</string>
    <string name="nav_settings">設定</string>
    <string name="log_dialog_title">実行ログ</string>
    <string name="log_dialog_close">閉じる</string>
    <string name="log_dialog_share">共有</string>
    <string name="custom_wallpaper_saved">カスタム壁紙を保存しました</string>
    <string name="need_storage_permission">ストレージ権限が必要です</string>
    <string name="permission_go_settings">設定へ</string>
    <string name="permission_cancel">キャンセル</string>
    <string name="permission_denied_title">ストレージ権限</string>
    <string name="permission_denied_message">権限が拒否されました。システム設定で手動で許可してください。</string>
    <string name="accessibility_enabled">アクセシビリティ有効</string>
    <string name="accessibility_disabled">アクセシビリティ無効（スイッチを押すと設定へ）</string>
    <string name="shizuku_install_hint">先にShizukuをインストールして起動してください</string>
    <string name="auto_overclock_title">自動リフレッシュレート</string>
    <string name="guard_enabled">自動ガード有効</string>
    <string name="guard_disabled">自動ガード無効</string>
    <string name="target_resolution_label">目標解像度</string>
    <string name="target_rate_label">目標レート</string>
    <string name="shizuku_not_running">Shizuku 未起動（先にインストール）</string>
    <string name="shizuku_no_perm">Shizuku 起動中、未認証</string>
    <string name="shizuku_authorized">Shizuku 認証済み</string>
    <string name="btn_authorize_shizuku">Shizuku を認証</string>
    <string name="accessibility_required">ガードを常駐させるためアクセシビリティを有効にしてください</string>
    <string name="accessibility_open_failed">アクセシビリティ設定を開けません</string>
    <string name="notification_perm_denied">通知権限が拒否されました、通知が表示されない場合があります</string>
    <string name="notification_channel_name">自動オーバークロックガード</string>
    <string name="notification_title">自動オーバークロックガード実行中</string>
    <string name="notification_stop">停止</string>
    <string name="current_rate_format" formatted="false">レート: %d Hz  解像度: %s</string>
    <string name="guard_running_format">ガード有効  %s</string>
    <string name="guard_watching_format">ガード中 @ %d Hz</string>
    <string name="guard_start_format" formatted="false">ガード開始、目標: %s @ %d Hz</string>
    <string name="guard_stopped">ガード停止</string>
    <string name="guard_no_res_format">解像度が見つかりません: %s</string>
    <string name="guard_target_updated" formatted="false">目標更新 → %s @ %d Hz</string>
    <string name="guard_res_fix">解像度修正、</string>
    <string name="guard_hz_up" formatted="false">%s%d Hz ↑ → %d Hz</string>
    <string name="guard_hz_down" formatted="false">%d Hz ↓ → %d Hz</string>
    <string name="show_refresh_rate_label">リフレッシュレート 表示/非表示</string>
    <string name="enabled_app_list_title">有効リスト</string>
    <string name="enabled_list_empty">有効なアプリがありません</string>
    <string name="app_management_title">アプリ管理</string>
    <string name="custom_app_refresh_desc">アプリ管理内のアプリ別リフレッシュレートに使用します。対象アプリを開くと、そのアプリに設定した解像度とリフレッシュレートへ自動で切り替えます。</string>
    <string name="auto_overclock_desc">全体のカスタムリフレッシュレートと解像度を選択するために使用します。システムやアプリが強制的に下げた場合、元の設定に戻します。</string>
    <string name="app_refresh_config_title">アプリ設定</string>
    <string name="enable_single_app_refresh">個別アプリのリフレッシュレートを有効化</string>
    <string name="single_app_refresh_desc">有効にすると、このアプリを開いた時に下で選択した解像度とリフレッシュレートへ切り替えます。</string>
    <string name="search_app_hint">アプリを検索</string>
    <string name="show_system_apps">システムアプリを表示</string>
    <string name="app_list_third_party_hint">初期状態ではサードパーティアプリのみ表示します。右上の三点メニューでシステムアプリを表示できます。</string>
    <string name="app_list_all_hint">サードパーティアプリとシステムアプリを表示中です。</string>
    <string name="no_apps_found">アプリが見つかりません</string>
    <string name="app_list_load_failed">アプリ一覧の読み込みに失敗しました。アプリ一覧アクセス権限を確認してください。</string>
    <string name="app_list_permission_title">アプリ一覧アクセス</string>
    <string name="app_list_permission_message">アプリ管理では、アプリ名とパッケージ名を表示するためにインストール済みアプリ一覧を読み取ります。システムで制限されている場合は、設定でこのアプリのアクセスを許可してください。</string>
    <string name="no_any_permission">権限が付与されていません。先に設定で Root または Shizuku 認証を選択してください。</string>
    <string name="custom_app_master_required">先に戻ってアプリ別リフレッシュレートのメインスイッチをオンにしてください</string>
    <string name="rate_lock_need_auth">先に Root または Shizuku 権限を許可してください</string>
    <string name="custom_notif_channel_name">アプリ別リフレッシュレート</string>
    <string name="custom_notif_stop">停止</string>
    <string name="custom_notif_status_on">[このアプリは独自リフレッシュレート 有効]</string>
    <string name="custom_notif_status_off">[このアプリは独自リフレッシュレート 未設定]</string>
    <string name="custom_notif_set_format">設定値  解像度: %1$s  リフレッシュ: %2$d Hz</string>
    <string name="custom_notif_current_format">現在の解像度: %1$s  現在のリフレッシュ: %2$d Hz</string>
    <string name="contributors_title">私たちについて</string>
    <string name="contrib_type_creator">種別: 制作者</string>
    <string name="contrib_info_aihaozhe">プロフィール: Coolapk 爱好者ahh</string>
    <string name="contrib_jump">プロフィールへ</string>
    <string name="contrib_type_coder">種別: コード貢献者 &amp; サポーター</string>
    <string name="contrib_info_shagua">プロフィール: Coolapk 傻瓜我爱你呀喵</string>
    <string name="contrib_type_supporter">種別: サポーター</string>
    <string name="contrib_info_huaiyin">プロフィール: Coolapk 槐蔭區</string>
    <string name="custom_notif_waiting_title">アプリ別リフレッシュレート · 実行中</string>
    <string name="custom_notif_waiting_text">有効なアプリへの切り替えを待機中…</string>
    <string name="perm_status_title">権限の許可状態</string>
    <string name="language_page_title">言語設定</string>
    <string name="lang_system">系统默认</string>
    <string name="lang_zh">简体中文</string>
    <string name="lang_zh_tw">繁體中文</string>
    <string name="lang_en">English</string>
    <string name="lang_ja">日本語</string>
    <string name="home_card_alpha_label">ホームカード透明度</string>
    <string name="custom_card_alpha_label">カスタムページ カード透明度</string>
    <string name="custom_text_alpha_label">カスタムページ 文字透明度</string>
    <string name="settings_title">設定</string>
    <string name="language_row_desc">アプリの表示言語を選択</string>
    <string name="check_update">アップデートを確認</string>
    <string name="update_available_title">アップデートがあります</string>
    <string name="update_available_msg">新しいバージョン %1$s が見つかりました</string>
    <string name="update_btn">更新</string>
    <string name="update_close">閉じる</string>
    <string name="update_latest_toast">最新バージョンです</string>
    <string name="update_checking_toast">アップデートを確認中</string>
    <string name="update_check_fail">確認に失敗しました</string>
    <string name="update_changelog_label">更新内容：</string>
    <string name="update_downloading">ダウンロード中</string>
    <string name="update_allow_install">不明なアプリのインストールを許可してから、もう一度更新を押してください</string>
</resources>
EOF


cat > app/src/main/res/values-zh-rTW/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">螢幕更新率</string>
    <string name="home_title">螢幕更新率</string>
    <string name="appearance_title">外觀</string>
    <string name="appearance_desc">桌布美化、檔位透明度、設定頁透明度</string>
    <string name="nav_scheme_title">導覽列樣式</string>
    <string name="nav_scheme_float">懸浮導覽</string>
    <string name="nav_scheme_classic">傳統導覽</string>
    <string name="nav_scheme_top">頂部導覽列</string>
    <string name="nav_scheme_left">左側導覽列</string>
    <string name="nav_scheme_right">右側導覽列</string>
    <string name="high_resolution">高解析度  </string>
    <string name="low_resolution">低解析度  </string>
    <string name="fallback_high_res">高解析度</string>
    <string name="fallback_low_res">低解析度</string>
    <string name="rate_name_supreme">鎏金超限</string>
    <string name="rate_name_limit">極限</string>
    <string name="rate_name_high">高刷</string>
    <string name="rate_name_extreme">極致</string>
    <string name="rate_name_excellent">極佳</string>
    <string name="rate_name_smooth">流暢</string>
    <string name="rate_name_standard">標準</string>
    <string name="rate_desc_supreme">金芒破限，視界抵達巔峰</string>
    <string name="rate_desc_limit">巔峰畫面質感，詮釋極限視覺體驗</string>
    <string name="rate_desc_high">畫面瞬時響應，無拖影卡頓</string>
    <string name="rate_desc_extreme">極致的畫面體驗，畫面快人一步</string>
    <string name="rate_desc_excellent">高等更新率，擁有超流暢的畫面體驗</string>
    <string name="rate_desc_smooth">高的更新率，畫面流暢</string>
    <string name="rate_desc_standard">常規更新率，延長續航</string>
    <string name="no_root_toast">請授權Root/Shizuku權限</string>
    <string name="switch_success">已切換至 %d Hz</string>
    <string name="switch_fail">切換失敗，請檢查 Root 授權</string>
    <string name="settings_root_title">授權狀態</string>
    <string name="settings_root_granted">已取得 Root 權限</string>
    <string name="settings_root_denied">未取得 Root 權限</string>
    <string name="settings_wallpaper_title">背景</string>
    <string name="wallpaper_gradient1">日落漸層</string>
    <string name="wp_sky">天藍</string>
    <string name="wp_pink">櫻粉</string>
    <string name="wp_mint">薄荷</string>
    <string name="wp_purple">暮紫</string>
    <string name="wallpaper_gradient2">極光漸層</string>
    <string name="wallpaper_gradient3">翡翠漸層</string>
    <string name="wallpaper_white">純白</string>
    <string name="wallpaper_black">純黑</string>
    <string name="wallpaper_custom">自訂背景</string>
    <string name="bg_clear">清除</string>
    <string name="bg_select">選擇</string>
    <string name="wallpaper_alpha_label">桌布透明度</string>
    <string name="card_display_title">檔位透明度</string>
    <string name="card_alpha_label">檔位透明度</string>
    <string name="settings_page_beautify">設定頁透明度</string>
    <string name="settings_card_alpha">卡片透明度</string>
    <string name="settings_text_alpha">文字透明度</string>
    <string name="app_page_alpha_title">自訂應用更新率頁</string>
    <string name="remote_page_alpha_title">遠端更新率頁</string>
    <string name="root_running_label">Root 執行</string>
    <string name="shizuku_running_label">Shizuku 執行</string>
    <string name="about_title">關於</string>
    <string name="version_label">版本: 1.6.5</string>
    <string name="generate_log_btn">產生記錄</string>
    <string name="language_zh">简体中文</string>
    <string name="language_zh_tw">繁體中文</string>
    <string name="language_en">English</string>
    <string name="language_ja">日本語</string>
    <string name="nav_home">首頁</string>
    <string name="nav_custom_app_refresh">自訂應用更新率</string>
    <string name="nav_remote">遠端更新率</string>
    <string name="remote_mode_wireless">無線連線</string>
    <string name="remote_mode_usb">USB連線</string>
    <string name="remote_ip_hint">裝置 IP 位址</string>
    <string name="remote_port_hint">連接埠</string>
    <string name="remote_pair_hint">配對碼</string>
    <string name="remote_connect_btn">連線</string>
    <string name="remote_status_idle">未連線</string>
    <string name="remote_status_none">未連線裝置</string>
    <string name="remote_modes_title">被控端更新率</string>
    <string name="remote_hint">在被控手機開啟「無線偵錯」，用「以配對碼配對裝置」取得 IP、連接埠和配對碼。</string>
    <string name="remote_usb_hint">用資料線連接被控裝置，並在被控端授權 USB 偵錯。</string>
    <string name="remote_status_need_input">請輸入 IP 位址和連接埠</string>
    <string name="remote_pair_port_hint">配對埠</string>
    <string name="remote_pair_btn">配對裝置</string>
    <string name="remote_section_pair">無線偵錯配對</string>
    <string name="remote_blur_ip">隱藏 IP 位址（點擊輸入欄檢視）</string>
    <string name="remote_max_devices">最多同時連接 3 台裝置</string>
    <string name="remote_section_connect">連線</string>
    <string name="remote_need_pair_input">請填寫 IP、配對埠和配對碼</string>
    <string name="remote_pairing">正在配對…</string>
    <string name="remote_pair_ok">配對成功，請填連接埠後點連線</string>
    <string name="remote_pair_fail">配對失敗：%1$s</string>
    <string name="remote_conn_wait">正在連線…</string>
    <string name="remote_connected">已連線 %1$s</string>
    <string name="remote_wireless_device">無線偵錯裝置</string>
    <string name="debug_mode_title">偵錯模式</string>
    <string name="anim_engine_title">動畫引擎</string>
    <string name="anim_effect_title">動畫效果</string>
    <string name="anim_engine_desc">頁面切換時使用滑順動畫</string>
    <string name="anim_type_title">動畫類型</string>
    <string name="anim_speed_title">動畫速度</string>
    <string name="anim_type_spring">物理彈簧</string>
    <string name="anim_type_linear">線性運動</string>
    <string name="anim_type_fade">淡入淡出</string>
    <string name="anim_type_slide">滑動</string>
    <string name="remote_conn_fail">連線失敗：%1$s</string>
    <string name="remote_switching">正在切換到 %1$d Hz…</string>
    <string name="remote_switch_ok">已切換到 %1$d Hz</string>
    <string name="remote_switch_fail">切換失敗：%1$s</string>
    <string name="remote_modes_parse_fail">無法讀取更新率，請確認裝置已連線</string>
    <string name="remote_history_title">歷史記錄</string>
    <string name="remote_switch_sent">已傳送（無法讀取目前更新率）</string>
    <string name="remote_switch_actual">已傳送，目前 %1$d Hz（可能被鎖定）</string>
    <string name="nav_settings">設定</string>
    <string name="log_dialog_title">執行記錄</string>
    <string name="log_dialog_close">關閉</string>
    <string name="log_dialog_share">分享</string>
    <string name="custom_wallpaper_saved">自訂桌布已儲存</string>
    <string name="need_storage_permission">需要儲存權限才能選擇圖片</string>
    <string name="permission_go_settings">前往設定</string>
    <string name="permission_cancel">取消</string>
    <string name="permission_denied_title">需要儲存權限</string>
    <string name="permission_denied_message">您已拒絕權限，請前往系統設定手動授予，否則無法選擇圖片。</string>
    <string name="accessibility_enabled">輔助服務已啟用</string>
    <string name="accessibility_disabled">輔助服務未啟用（點擊開關將引導啟用）</string>
    <string name="shizuku_install_hint">請先安裝並啟動 Shizuku</string>
    <string name="auto_overclock_title">螢幕更新率自動超頻</string>
    <string name="guard_enabled">自動啟動守護已開啟</string>
    <string name="guard_disabled">自動啟動守護未開啟</string>
    <string name="target_resolution_label">目標解析度</string>
    <string name="target_rate_label">目標更新率</string>
    <string name="shizuku_not_running">Shizuku 未執行（請先安裝啟動）</string>
    <string name="shizuku_no_perm">Shizuku 執行中，未授權</string>
    <string name="shizuku_authorized">Shizuku 已授權</string>
    <string name="btn_authorize_shizuku">授權 Shizuku</string>
    <string name="accessibility_required">請先開啟輔助服務以確保守護常駐</string>
    <string name="accessibility_open_failed">無法開啟輔助功能設定</string>
    <string name="notification_perm_denied">通知權限被拒絕，守護通知可能無法顯示</string>
    <string name="notification_channel_name">自動超頻守護</string>
    <string name="notification_title">自動超頻守護執行中</string>
    <string name="notification_stop">停止</string>
    <string name="current_rate_format" formatted="false">當前更新率: %d Hz  解析度: %s</string>
    <string name="guard_running_format">自動啟動守護已開啟  %s</string>
    <string name="guard_watching_format">守護中 @ %d Hz</string>
    <string name="guard_start_format" formatted="false">守護啟動，目標：%s @ %d Hz</string>
    <string name="guard_stopped">守護已停止</string>
    <string name="guard_no_res_format">未找到解析度 %s</string>
    <string name="guard_target_updated" formatted="false">目標已更新 → %s @ %d Hz</string>
    <string name="guard_res_fix">解析度校正，</string>
    <string name="guard_hz_up" formatted="false">%s%d Hz ↑ → %d Hz</string>
    <string name="guard_hz_down" formatted="false">%d Hz ↓ → %d Hz</string>
    <string name="show_refresh_rate_label">顯示/隱藏更新率</string>
    <string name="enabled_app_list_title">啟用名單</string>
    <string name="enabled_list_empty">目前尚無已啟用的應用程式</string>
    <string name="app_management_title">應用管理</string>
    <string name="custom_app_refresh_desc">用於應用管理裡面的自訂應用更新率。打開目標應用時，軟體會自動切到你在應用管理裡為該應用選擇的解析度和更新率。</string>
    <string name="auto_overclock_desc">用於全域選擇自訂更新率與解析度。當系統或應用強制降低解析度或更新率時，軟體將幫你拉回原本設定的更新率與解析度。</string>
    <string name="app_refresh_config_title">應用設定</string>
    <string name="enable_single_app_refresh">啟用單獨應用更新率</string>
    <string name="single_app_refresh_desc">開啟後，進入這個應用時會自動切換到下面選擇的解析度和更新率。</string>
    <string name="search_app_hint">搜尋應用</string>
    <string name="show_system_apps">顯示系統應用</string>
    <string name="app_list_third_party_hint">預設僅顯示第三方應用，點右上角三個點可顯示系統應用。</string>
    <string name="app_list_all_hint">目前正在顯示第三方應用和系統應用。</string>
    <string name="no_apps_found">找不到應用</string>
    <string name="app_list_load_failed">讀取應用列表失敗，請檢查存取應用列表權限</string>
    <string name="app_list_permission_title">存取應用列表權限</string>
    <string name="app_list_permission_message">應用管理需要讀取已安裝應用列表，才能顯示軟體名稱和軟體包名。如果系統限制了此權限，請在系統設定中允許本應用存取應用列表。</string>
    <string name="no_any_permission">你沒有賦予任何權限，請先在設定裡選擇 Root 或 Shizuku 授權。</string>
    <string name="custom_app_master_required">請先返回開啟自訂應用更新率總開關</string>
    <string name="rate_lock_need_auth">請先在設定裡授予 Root 或 Shizuku 權限</string>
    <string name="custom_notif_channel_name">指定應用更新率</string>
    <string name="custom_notif_stop">停止</string>
    <string name="custom_notif_status_on">[該應用已啟用自訂更新率]</string>
    <string name="custom_notif_status_off">[該應用未啟用自訂更新率]</string>
    <string name="custom_notif_set_format">已設定  解析度: %1$s  更新率: %2$d Hz</string>
    <string name="custom_notif_current_format">目前解析度: %1$s  目前更新率: %2$d Hz</string>
    <string name="contributors_title">關於我們</string>
    <string name="contrib_type_creator">貢獻類型: 創作者</string>
    <string name="contrib_info_aihaozhe">個人資訊: 酷安 爱好者ahh</string>
    <string name="contrib_jump">前往關注</string>
    <string name="contrib_type_coder">貢獻類型: 程式碼貢獻者&amp;支持者</string>
    <string name="contrib_info_shagua">個人資訊: 酷安 傻瓜我爱你呀喵</string>
    <string name="contrib_type_supporter">貢獻類型: 支持者</string>
    <string name="contrib_info_huaiyin">個人資訊: 酷安 槐蔭區</string>
    <string name="custom_notif_waiting_title">自訂應用更新率 · 執行中</string>
    <string name="custom_notif_waiting_text">等待切換到已啟用的應用…</string>
    <string name="perm_status_title">權限授權狀態</string>
    <string name="language_page_title">語言設定</string>
    <string name="lang_system">系统默认</string>
    <string name="lang_zh">简体中文</string>
    <string name="lang_zh_tw">繁體中文</string>
    <string name="lang_en">English</string>
    <string name="lang_ja">日本語</string>
    <string name="home_card_alpha_label">主頁卡片透明度</string>
    <string name="custom_card_alpha_label">自訂頁卡片透明度</string>
    <string name="custom_text_alpha_label">自訂頁文字透明度</string>
    <string name="settings_title">設定</string>
    <string name="language_row_desc">選擇應用顯示語言</string>
    <string name="check_update">檢查更新</string>
    <string name="update_available_title">有可用更新</string>
    <string name="update_available_msg">偵測到新版本 %1$s</string>
    <string name="update_btn">更新</string>
    <string name="update_close">關閉</string>
    <string name="update_latest_toast">目前已是最新版本</string>
    <string name="update_checking_toast">正在檢查更新</string>
    <string name="update_check_fail">檢查更新失敗，請稍後重試</string>
    <string name="update_changelog_label">更新內容：</string>
    <string name="update_downloading">正在下載更新</string>
    <string name="update_allow_install">請先允許安裝未知應用權限，然後重新點擊更新</string>
</resources>
EOF

cat > app/src/main/res/values/colors.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#1976D2</color><color name="primary_dark">#1565C0</color><color name="accent">#42A5F5</color>
    <color name="white">#FFFFFFFF</color><color name="black">#FF000000</color><color name="bg_main">#F0F4FF</color>
    <color name="text_dark">#1A1A2E</color><color name="text_muted">#888888</color>
</resources>
EOF

cat > app/src/main/res/values/themes.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.RefreshRate" parent="Theme.MaterialComponents.Light.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryDark">@color/primary_dark</item>
        <item name="colorAccent">@color/accent</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
    </style>
</resources>
EOF
SCRIPTEOF
chmod +x generate.sh
./generate.sh
