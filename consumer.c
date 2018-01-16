#include "k_v_benchmark.h"
#include <zmq.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <assert.h>
#include <inttypes.h>

bm_type_t bm_type = BM_NONE;
int BM_MPSC_OQ_CAP = -1;

bm_process_op_t bm_process_op_type = BM_PROCESS_DUMMY;
int SPIN_TIME = -1;
int random_accum = 0;

int get_and_set_config_from_file() {
    static char* filename = "./bm_config.txt";
    FILE* bm_config_fptr = fopen(filename, "r");
    if (bm_config_fptr == NULL) {
        fprintf(stderr, "%s does NOT exist or it is Wrong.\n", filename);
        return -1;
    }

    char line[50];
    fgets(line, 50, bm_config_fptr);
    bm_type = atoi(line);
    fgets(line, 50, bm_config_fptr);
    BM_MPSC_OQ_CAP = atoi(line);
    fgets(line, 50, bm_config_fptr);
    bm_process_op_type = atoi(line);
    fgets(line, 50, bm_config_fptr);
    SPIN_TIME = atoi(line);
    fclose(bm_config_fptr);
    return 0;
}

void bm_process_op(bm_op_t op) {
    switch(bm_process_op_type) {
        case BM_PROCESS_DUMMY: {
            ;
        } break;
        case BM_PROCESS_ADD: {
            random_accum += rand();
        } break;
        case BM_PROCESS_SPIN: {
            struct timeval t1, t2;
            gettimeofday(&t1, NULL);
            double elapsed = 0;
            do {
                gettimeofday(&t2, NULL);
                elapsed = t2.tv_usec - t1.tv_usec;
            } while(elapsed < SPIN_TIME);
        } break;
        case BM_PROCESS_PRINT: {
            fprintf(stderr, "type: %d, key: %"PRIu64"\n", op.type, op.key_hv);
        } break;
    }
}

int main (void)
{
    int rc = get_and_set_config_from_file();
    if (rc < 0) return;

    void* context = zmq_ctx_new ();
    void* consumer = zmq_socket (context, ZMQ_SUB);
    zmq_connect (consumer, "tcp://localhost:5555");
    zmq_setsockopt(consumer, ZMQ_SUBSCRIBE, NULL, 0);
    fprintf (stderr, "Connected consumer...\n");

    while (1) {
        char buffer[sizeof(bm_op_t)];
        int nbytes = zmq_recv(consumer, buffer, sizeof(bm_op_t), ZMQ_DONTWAIT);
        if (sizeof(bm_op_t) == nbytes) {
            bm_op_t* op_ptr = (bm_op_t*) buffer;
            bm_process_op(*op_ptr);
        }
    }
    return 0;
}