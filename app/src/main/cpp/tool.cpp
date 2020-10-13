/*
 * 这个C++代码本意是编译成一个工具让DAE命令行调用，用于告知Xposed模块啥时候开始Hook啥App、啥时候停止的，代码写的比较烂，毕竟很久没碰C++了。
 * 后来一拍脑门，哎，发广播岂不更好？于是就改成发广播了😂
 * */

#include <iostream>
#include <string>
#include <fstream>
using namespace std;

//const string gPkgname = "com.softsec.mobsec.dae.apimonitor";
const string gPrfspath = "/data/data/com.softsec.mobsec.dae.apimonitor/shared_prefs/DAEAM_SP.xml";
const string gStringPrefix = "<string name=\"";
const string gStringSuffix = "</string>";

const string gBooleanPrefix = "<boolean name=\"";
const string gBooleanSuffix =" value=\"false\" />";
//const string gPathLog = "";
const string gPathTempLog = "";

enum output_type{
    ERROR, INFO, WARNING
};

void output(output_type type, string outputContent) {
    switch(type) {
        case ERROR :
            cout << "[ERROR]" << outputContent;
            break;
        case INFO :
            cout << "[INFO]" << outputContent;
            break;
        case WARNING :
            cout << "[WARNING]" << outputContent;
            break;
        default:
            return;
    }
    cout << endl;
}

void writeFile(string* fileLines,int lineSize) {
    ofstream ofs("/data/data/com.softsec.mobsec.dae.apimonitor/shared_prefs/DAEAM_SP.xml", ios::app);
    for(int i = 0; i < lineSize; i++)
        ofs << fileLines[i] << endl;
    ofs.close();
}

void AddPkgName(string pkgName, string logDir, string *fileLines, int appLineNum, int lineSize) {
    if(appLineNum != 0){
        string appsToHookLine = fileLines[appLineNum];
        appsToHookLine.insert(appsToHookLine.find_first_of(">") + 1, pkgName + ";");
        fileLines[appLineNum] = appsToHookLine;
    } else {
        fileLines[lineSize] = fileLines[lineSize - 1];
//        <string name="SP_EX_APPS_TO_HOOK">com.ss.android.article.news;philm.vilo.im;</string>
        fileLines[lineSize - 1] = gStringPrefix + "SP_APPS_TO_HOOK\">" + pkgName + ";" + gStringSuffix;
        appLineNum = lineSize - 1;
        lineSize++;
    }
    fileLines[lineSize + 1] = fileLines[lineSize - 1];
    // <boolean name="com.ss.android.article.news_HAS_W_PERMISSION_KEY" value="false" />
    fileLines[lineSize - 1] = gBooleanPrefix + pkgName + "_HAS_W_PERMISSION_KEY\"" + gBooleanSuffix;
    // <string name="com.tencent.mm_LOG_DIR">/data/user/0/com.tencent.mm/DAEAM_testing/com.tencent.mm</string>
    fileLines[lineSize] = gStringPrefix + pkgName + "_LOG_DIR\">" + logDir + "/DAEAM_testing/" + pkgName + gStringSuffix;
    lineSize += 2;
    writeFile(fileLines, lineSize);
}

void RemovePkgName(string pkgName, string *fileLines, int appLineNum, int lineSize, int exLine) {
    string appsToHookLine = fileLines[appLineNum];
    unsigned int index;
    if((index = appsToHookLine.find(pkgName + ";")) != string::npos)
        appsToHookLine.erase(index, pkgName.size() + 1);
    else {
        output(ERROR, pkgName + " not found.");
        exit(1);
    }
    //<string name="SP_EX_APPS_TO_HOOK">com.ss.android.article.news;philm.vilo.im;</string>
    if(exLine == -1)
        fileLines[lineSize++] = "<string name=\"SP_EX_APPS_TO_HOOK\">" + pkgName + ";</string>";
     else {
        string tmp = fileLines[exLine];
        tmp = tmp.substr(0, tmp.rfind("<")) + pkgName + ";</string>";
    }
    fileLines[appLineNum] = appsToHookLine;
    for(int i = lineSize - 1; i >= 0; i--) {
        if((fileLines[i].find(pkgName) != string::npos)) {
            if(fileLines[i].find("_LOG_DIR") != string::npos) {
                unsigned int from = fileLines[i].find_first_of(">") + 1;
                unsigned int to = fileLines[i].find_last_of(">");
                ifstream fin(gPathTempLog);
                if(!fin) system(("mkdirs " + gPathTempLog).c_str());
                string cmd = "mv " + fileLines[i].substr(from, to - from) + " /sdcard/DAETempLogs/" + pkgName;
            }
            if(i != lineSize - 1) for(int j = i; j < lineSize - 1; j++) fileLines[j] = fileLines[j + 1];
            fileLines[lineSize - 1] = "";
            lineSize--;
        }
    }

    writeFile(fileLines, lineSize);
}

void StopHook(string *fileLines, int applineNum, int lineSize) {
//    for(int i = applineNum; i < lineSize - 2; i++) fileLines[i] = fileLines[i + 1];
//    fileLines[lineSize - 1] = "";
//    lineSize--;
    writeFile(fileLines, lineSize);
}

void ReadFile(string* fileLines, int &appLineNum, int &lineSize, int &exLine) {
    ifstream ifs;
    ifs.open(gPrfspath.c_str(), ios::binary);
    string line;
    string* lines = new string[100];
    int i = 0;
    appLineNum = 0;
    exLine = -1;
    while(getline(ifs, line)){
        lines[i] = line;
        if(line.find("SP_APPS_TO_HOOK_KEY") != string::npos) appLineNum = i;
        if(line.find("SP_EX_APPS_TO_HOOK_KEY") != string::npos) exLine = i;
        i++;
    }
    fileLines = lines;
    ifs.close();
    lineSize = i;
}



int main(int argc,char *argv[]) {

    if(argc < 2) {
//        help();
        return 1;
    }

    int appLineNum;
    int lineSize;
    int exLine;
    string *fileLines = nullptr;
    for(int i = 1; i < argc; i += 2) {
        if(!strcmp(argv[i], "-a")||!strcmp(argv[i], "--add")) {
            if(argc <= i + 2 || strcmp(argv[i + 2], "--logdir"))
                output(ERROR, "Need to specify logdir, use --logdir PATH.");
            else {
                ReadFile(fileLines, appLineNum, lineSize, exLine);
                AddPkgName((string) argv[i + 1], (string) argv[i + 3], fileLines, appLineNum, lineSize);
                i += 2;
            }
        }

        else if(!strcmp(argv[i], "-r")) {
            ReadFile(fileLines, appLineNum, lineSize, exLine);
            RemovePkgName((string) argv[i + 1], fileLines, appLineNum, lineSize, exLine);
        }
        else if(!strcmp(argv[i], "-s")) {
            ReadFile(fileLines, appLineNum, lineSize, exLine);
            StopHook(fileLines, appLineNum, lineSize);
        }
    }
    return 0;
}