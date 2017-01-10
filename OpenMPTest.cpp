//
// Created by Lavenger on 11/20/2016.
//

#include "omp.h"
#include "stdio.h"

void parallel_region();

void work_sharing();

int main() {

    parallel_region();
    work_sharing();
    return 0;
}

void work_sharing() {
    printf("\nwork sharing\n");
#pragma omp parallel
//#pragma omp for
#pragma omp for schedule(static)
    for (int i = 0; i < 4; ++i) {
        printf("%d\n", i);
    }
    printf("\nwork sharing\n");
}

void parallel_region() {

    omp_set_num_threads(4);
#pragma omp parallel
    {
        int id = omp_get_thread_num();
        printf("Thread" + id);
    }
}