#include <jni.h>
#include <string>
#include <android/log.h>

#define  LOG_TAG    "test===="
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

/**
 * NALU头由一个字节组成,它的语法如下:
      +---------------+
      |0|1|2|3|4|5|6|7|
      +-+-+-+-+-+-+-+-+
      |F|NRI|  Type   |
      +---------------+
      F: 1个比特（bit）.
        forbidden_zero_bit. 在 H.264 规范中规定了这一位必须为 0.
      NRI: 2个比特（bit）.
        nal_ref_idc. 取00~11,似乎指示这个NALU的重要性,如00的NALU解码器可以丢弃它而不影响图像的回放,0～3，取值越大，表示当前NAL越重要，需要优先受到保护。如果当前NAL是属于参考帧的片，或是序列参数集，或是图像参数集这些重要的单位时，本句法元素必需大于0。
      Type: 5个比特（bit）.
        标识NAL单元中的RBSP数据类型，其中，nal_unit_type为1， 2， 3， 4， 5的NAL单元称为VCL的NAL单元，其他类型的NAL单元为非VCL的NAL单元。
 */
typedef enum {//1～12由H.264使用，24～31由H.264以外的应用使用
    NALU_TYPE_SLICE = 1,//不分区，非IDR图像的片
    NALU_TYPE_DPA = 2,//片分区A
    NALU_TYPE_DPB = 3,//片分区B
    NALU_TYPE_DPC = 4,//片分区C
    NALU_TYPE_IDR = 5,//IDR图像中的片
    NALU_TYPE_SEI = 6,//补充增强信息单元（SEI）
    NALU_TYPE_SPS = 7,//序列参数集，包含的是针对一连续编码视频序列的参数，如标识符 seq_parameter_set_id、帧数及 POC 的约束、参考帧数目、解码图像尺寸和帧场编码模式选择标识等等
    NALU_TYPE_PPS = 8,//图像参数集，对应的是一个序列中某一幅图像或者某几幅图像，其参数如标识符pic_parameter_set_id、可选的 seq_parameter_set_id、熵编码模式选择标识、片组数目、初始量化参数和去方块滤波系数调整标识等等
    NALU_TYPE_AUD = 9,//序列结束
    NALU_TYPE_EOSEQ = 10,//序列结束
    NALU_TYPE_EOSTREAM = 11,//码流借宿
    NALU_TYPE_FILL = 12,//填充
} NaluType;
typedef enum {
    NALU_PRIORITY_DISPOSABLE = 0,
    NALU_PRIORITY_LOW = 1,
    NALU_PRIORITY_HIGH = 2,
    NALU_PRIORITY_HIGHEST = 3,
} NaluPriority;
typedef struct {
    int startcodeprefix_len;
    unsigned len;//一个NALU单元的长度，不包括startcode
    unsigned max_size;//一个NALU单元字节序列长度
    int forbidden_bit;//在 H.264 规范中规定了这一位必须为 0.
    int nal_reference_idc;//表示NAL的优先级。0～3，取值越大，表示当前NAL越重要，需要优先受到保护。如果当前NAL是属于参考帧的片，或是序列参数集，或是图像参数集这些重要的单位时，本句法元素必需大于0。
    int nal_unit_type;//标识NAL单元中的RBSP数据类型，其中，nal_unit_type为1， 2， 3， 4， 5的NAL单元称为VCL的NAL单元，其他类型的NAL单元为非VCL的NAL单元。
    char *buf;//包含EBSP后面的第一个字节 也就是 RBSP
} NALU_t;
FILE *h264bitstream = NULL;
int info2 = 0, info3 = 0;

/**
 * start code 是3位时 即Slice中的startcode
 * @param Buf
 * @return
 */
static int FindStartCode2(unsigned char *Buf) {
    if (Buf[0] != 0 || Buf[1] != 0 || Buf[2] != 1)return 0;////0x000001?
    else return 1;
}

/**
 * start code 是4位时 即Slice中第一个startcode
 * @param Buf
 * @return
 */
static int FindStartCode3(unsigned char *Buf) {
    if (Buf[0] != 0 || Buf[1] != 0 || Buf[2] != 0 || Buf[3] != 1)return 0;//0x00000001?
    else return 1;
}

int GetAnnexbNALU(NALU_t *nalu) {
    int pos = 0;
    int StartCodeFound, rewind;
    unsigned char *Buf;

    if ((Buf = (unsigned char *) calloc(nalu->max_size, sizeof(char))) == NULL)
        LOGI("GetAnnexbNALU: Could not allocate Buf memory\n");

    nalu->startcodeprefix_len = 3;

    if (3 != fread(Buf, 1, 3, h264bitstream)) {
        free(Buf);
        return 0;
    }
    info2 = FindStartCode2(Buf);
    if (info2 != 1) {
        if (1 != fread(Buf + 3, 1, 1, h264bitstream)) {
            free(Buf);
            return 0;
        }
        info3 = FindStartCode3(Buf);
        if (info3 != 1) {
            free(Buf);
            return -1;
        } else {
            pos = 4;
            nalu->startcodeprefix_len = 4;
        }
    } else {
        nalu->startcodeprefix_len = 3;
        pos = 3;
    }
    StartCodeFound = 0;
    info2 = 0;
    info3 = 0;

    while (!StartCodeFound) {
        if (feof(h264bitstream)) {
            nalu->len = (pos - 1) - nalu->startcodeprefix_len;
            memcpy(nalu->buf, &Buf[nalu->startcodeprefix_len], nalu->len);
            nalu->forbidden_bit = nalu->buf[0] & 0x80; //1 bit
            nalu->nal_reference_idc = nalu->buf[0] & 0x60; // 2 bit
            nalu->nal_unit_type = (nalu->buf[0]) & 0x1f;// 5 bit
            free(Buf);
            return pos - 1;
        }
        Buf[pos++] = fgetc(h264bitstream);
        info3 = FindStartCode3(&Buf[pos - 4]);
        if (info3 != 1)
            info2 = FindStartCode2(&Buf[pos - 3]);
        StartCodeFound = (info2 == 1 || info3 == 1);
    }

    // Here, we have found another start code (and read length of startcode bytes more than we should
    // have.  Hence, go back in the file
    rewind = (info3 == 1) ? -4 : -3;

    if (0 != fseek(h264bitstream, rewind, SEEK_CUR)) {
        free(Buf);
        LOGI("GetAnnexbNALU: Cannot fseek in the bit stream file");
    }

    // Here the Start code, the complete NALU, and the next start code is in the Buf.
    // The size of Buf is pos, pos+rewind are the number of bytes excluding the next
    // start code, and (pos+rewind)-startcodeprefix_len is the size of the NALU excluding the start code

    nalu->len = (pos + rewind) - nalu->startcodeprefix_len;
    memcpy(nalu->buf, &Buf[nalu->startcodeprefix_len], nalu->len);//
    nalu->forbidden_bit = nalu->buf[0] & 0x80; //1 bit
    nalu->nal_reference_idc = nalu->buf[0] & 0x60; // 2 bit
    nalu->nal_unit_type = (nalu->buf[0]) & 0x1f;// 5 bit
    free(Buf);

    return (pos + rewind);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_shiwei_vm_vm13_MainActivity_startAnalysis(JNIEnv *env, jobject thiz, jstring path) {
    NALU_t *n;
    int buffersize = 100000;
    FILE *myout = stdout;

    const char *url = env->GetStringUTFChars(path, 0);
    h264bitstream = fopen(url, "rb+");
    if (h264bitstream == NULL) {
        LOGI("Open file error");
        return;
    }
    n = (NALU_t *) calloc(1, sizeof(NALU_t));
    if (n == NULL) {
        LOGI("Alloc NALU Error");
        return;
    }
    n->max_size = buffersize;
    n->buf = (char *) calloc(buffersize, sizeof(char));
    if (n->buf == NULL) {
        free(n);
        LOGI("AllocNALU:n->buf");
        return;
    }
    int data_offset = 0;
    int nal_num = 0;
    LOGI("-----+-------- NALU Table ------+---------+\n");
    LOGI(" NUM |    POS  |    IDC |  TYPE |   LEN   |\n");
    LOGI("-----+---------+--------+-------+---------+\n");
    while (!feof(h264bitstream)) {
        int data_lenth;
        data_lenth = GetAnnexbNALU(n);
        char type_str[20] = {0};
        switch (n->nal_unit_type) {
            case NALU_TYPE_SLICE:
                LOGI(type_str, "SLICE");
                break;
            case NALU_TYPE_DPA:
                LOGI(type_str, "DPA");
                break;
            case NALU_TYPE_DPB:
                LOGI(type_str, "DPB");
                break;
            case NALU_TYPE_DPC:
                LOGI(type_str, "DPC");
                break;
            case NALU_TYPE_IDR:
                LOGI(type_str, "IDR");
                break;
            case NALU_TYPE_SEI:
                LOGI(type_str, "SEI");
                break;
            case NALU_TYPE_SPS:
                LOGI(type_str, "SPS");
                break;
            case NALU_TYPE_PPS:
                LOGI(type_str, "PPS");
                break;
            case NALU_TYPE_AUD:
                LOGI(type_str, "AUD");
                break;
            case NALU_TYPE_EOSEQ:
                LOGI(type_str, "EOSEQ");
                break;
            case NALU_TYPE_EOSTREAM:
                LOGI(type_str, "EOSTREAM");
                break;
            case NALU_TYPE_FILL:
                LOGI(type_str, "FILL");
                break;
        }
        char idc_str[20] = {0};
        switch (n->nal_reference_idc >> 5) {
            case NALU_PRIORITY_DISPOSABLE:
                LOGI(idc_str, "DISPOS");
                break;
            case NALU_PRIORITY_LOW:
                LOGI(idc_str, "LOW");
                break;
            case NALU_PRIORITY_HIGH:
                LOGI(idc_str, "HIGH");
                break;
            case NALU_PRIORITY_HIGHEST:
                LOGI(idc_str, "HIGHEST");
                break;
        }
        fprintf(myout, "%5d| %8d| %7s| %6s| %8d|\n", nal_num, data_offset, idc_str, type_str,
                n->len);
        data_offset = data_offset + data_lenth;

        nal_num++;
    }
    if (n) {
        if (n->buf) {
            free(n->buf);
            n->buf = NULL;
        }
        free(n);
    }

    env->ReleaseStringUTFChars(path, url);

}




