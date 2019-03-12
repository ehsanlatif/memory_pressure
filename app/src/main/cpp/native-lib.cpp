#include <jni.h>
#include <string>
#include <sys/resource.h>
#include <linux/resource.h>
#include <zconf.h>
#include <android/log.h>
#include <vector>
#include <fstream>
#include <dirent.h>

using namespace std;
static vector<string> s;

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_MySystemService_PassSizeToNative(
        JNIEnv* env,
        jobject instance,
        jint size,
        jboolean repeat) {
    // std::string hello = "Hello from C++";
    setuid(0);
    nice(-17);

//    int ArraySize=size;
//
//    char *process_name="com.google.android.gm";
//
//    int id;
//    pid_t pid = -1;
//    DIR* dir;
//    FILE *fp;
//    char filename[32];
//    char cmdline[256];
//
//    struct dirent * entry;
//
//    if (process_name == NULL)
//        pid= -1;
//
//    dir = opendir("/proc");
//    if (dir == NULL)
//        pid= -1;
//
//    while((entry = readdir(dir)) != NULL) {
//        id = atoi(entry->d_name);
//        if (id != 0) {
//            sprintf(filename, "/proc/%d/cmdline", id);
//            fp = fopen(filename, "r");
//            if (fp) {
//                fgets(cmdline, sizeof(cmdline), fp);
//                fclose(fp);
//
//                if (strcmp(process_name, cmdline) == 0) {
//                    /* process found */
//                    pid = id;
//                    break;
//                }
//            }
//        }
//    }
//
//    closedir(dir);


//    FILE* file = fopen("/sdcard/hello.csv","wb");
//
//    if (file != NULL)
//    {

    //for(int i=0;i<10;i++) {
    if (!repeat)
        s.clear();

    string sstr(size, ' ');
    s.push_back(sstr);

//            int tSize = 0, resident = 0, share = 0;
//            int tSize1 = 0, resident1 = 0, share1 = 0;
//            ifstream buffer("/proc/self/statm");
//            buffer >> tSize >> resident >> share;
//            buffer.close();
//            const char * pid_char=to_string(id).c_str();
//            char proc_chrome_dir[1024]="/proc/";
//            strcat(proc_chrome_dir,pid_char);
//            strcat(proc_chrome_dir,"/statm");
//            ifstream buffer1(proc_chrome_dir);
//            buffer1 >> tSize1 >> resident1 >> share1;
//            buffer1.close();
//
//            long page_size_kb = sysconf(_SC_PAGE_SIZE) / 1024; // in case x86-64 is configured to use 2MB pages
//            double self_rss = resident * page_size_kb;
//
//            double self_shared_mem = share * page_size_kb;
//
//            double chrome_rss = resident1 * page_size_kb;
//
//            double chrome_shared_mem = share1 * page_size_kb;
//
//            __android_log_print(ANDROID_LOG_ERROR, "TRACKERS", "Self_PSS: %f \t Gmail_PSS: %f",(self_rss - self_shared_mem), (chrome_rss - chrome_shared_mem));
//            fprintf(file, "%f,%f\n",(self_rss - self_shared_mem), (chrome_rss - chrome_shared_mem));
//            //usleep(1000);
//        //}
//        fflush(file);
//        fclose(file);
    //}
    //return env->NewStringUTF(hello.c_str());
}


