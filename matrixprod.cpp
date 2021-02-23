//#include <omp.h>
#include <papi.h>
#include <stdio.h>
#include <time.h>

#include <cstdlib>
#include <iomanip>
#include <iostream>

using namespace std;

#define SYSTEMTIME clock_t

void OnMult(int m_ar, int m_br) {
  SYSTEMTIME Time1, Time2;

  char st[100];
  double temp;
  int i, j, k;

  double *pha, *phb, *phc;

  pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
  phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
  phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

  for (i = 0; i < m_ar; i++)
    for (j = 0; j < m_ar; j++) pha[i * m_ar + j] = (double)1.0;

  for (i = 0; i < m_br; i++)
    for (j = 0; j < m_br; j++) phb[i * m_br + j] = (double)(i + 1);

  Time1 = clock();

  for (i = 0; i < m_ar; i++) {
    for (j = 0; j < m_br; j++) {
      temp = 0;
      for (k = 0; k < m_ar; k++) {
        temp += pha[i * m_ar + k] * phb[k * m_br + j];
      }
      phc[i * m_ar + j] = temp;
    }
  }

  Time2 = clock();
  sprintf(st, "Time: %3.3f seconds\n",
          (double)(Time2 - Time1) / CLOCKS_PER_SEC);
  cout << st;

  cout << "Result matrix: " << endl;
  for (i = 0; i < 1; i++) {
    for (j = 0; j < min(10, m_br); j++) cout << phc[j] << " ";
  }
  cout << endl;

  free(pha);
  free(phb);
  free(phc);
}

void OnMultLine(int m_ar, int m_br) {}

float produtoInterno(float *v1, float *v2, int col) {
  int i;
  float soma = 0.0;

  for (i = 0; i < col; i++) soma += v1[i] * v2[i];

  return (soma);
}

void handle_error(int retval) {
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}

int main(int argc, char *argv[]) {
  char c;
  int lin, col, nt = 1;
  int op;

  int EventSet = PAPI_NULL;
  long long values[2];
  int ret;

  ret = PAPI_library_init(PAPI_VER_CURRENT);
  if (ret != PAPI_VER_CURRENT) cout << "FAIL" << endl;

  ret = PAPI_create_eventset(&EventSet);
  if (ret != PAPI_OK) cout << "ERRO: create eventset" << endl;

  ret = PAPI_add_event(EventSet, PAPI_L1_DCM);
  if (ret != PAPI_OK) cout << "ERRO: PAPI_L1_DCM" << endl;

  ret = PAPI_add_event(EventSet, PAPI_L2_DCM);
  if (ret != PAPI_OK) cout << "ERRO: PAPI_L2_DCM" << endl;

  op = 1;
  do {
    cout << endl << "1. Multiplication" << endl;
    cout << "2. Line Multiplication" << endl;
    cout << "Selection?: ";
    cin >> op;
    if (op == 0) break;
    printf("Dimensions: lins cols ? ");
    cin >> lin >> col;

    // Start counting
    ret = PAPI_start(EventSet);
    if (ret != PAPI_OK) cout << "ERRO: Start PAPI" << endl;

    switch (op) {
      case 1:
        OnMult(lin, col);
        break;
      case 2:
        OnMultLine(lin, col);

        break;
    }

    ret = PAPI_stop(EventSet, values);
    if (ret != PAPI_OK) cout << "ERRO: Stop PAPI" << endl;
    printf("L1 DCM: %lld \n", values[0]);
    printf("L2 DCM: %lld \n", values[1]);

    ret = PAPI_reset(EventSet);
    if (ret != PAPI_OK) cout << "FAIL reset" << endl;

  } while (op != 0);

  ret = PAPI_remove_event(EventSet, PAPI_L1_DCM);
  if (ret != PAPI_OK) cout << "FAIL remove event" << endl;

  ret = PAPI_remove_event(EventSet, PAPI_L2_DCM);
  if (ret != PAPI_OK) cout << "FAIL remove event" << endl;

  ret = PAPI_destroy_eventset(&EventSet);
  if (ret != PAPI_OK) cout << "FAIL destroy" << endl;
}