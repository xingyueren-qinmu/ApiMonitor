## DAE调用API Mnitor

### 【开始Hook】

```java
sendBroadcast(Intent("ACTION_DAE_TASK")
    .putExtra("INTENT_BC_FROM", "com.softsec.mobsec.dae")
    .putExtra("INTENT_DAE_BC_PKGNAME", "philm.vilo.im")
    .putExtra("INTENT_DAE_BC_TEST_START", true)
);
```

###【结束Hook】

```java
sendBroadcast(Intent("ACTION_DAE_TASK")
    .putExtra("INTENT_BC_FROM", "com.softsec.mobsec.dae")
    .putExtra("INTENT_DAE_BC_PKGNAME", "philm.vilo.im")
    .putExtra("INTENT_DAE_BC_TEST_START", false)
);
```

### 【接收结果】

```java
if(intent?.action == "ACTION_DAE_APIMONITOR_RES") {
    String resultPath = intent.getStringExtra("INTENT_BC_APIMONITOR_RES_PATH");
}
```

