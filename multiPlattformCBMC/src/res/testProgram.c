//windows doesn't care about spaces in the includes, but my Linux (Ubuntu)
//needs it WITHOUT any spaces. Otherwise it will fail.
#include <stdlib.h>
int nondet_uint();
#include <stdint.h>
#ifndef LENGTH
#define LENGTH 5
#endif

int main ( int argc , char *argv[]) {
	unsigned int a[LENGTH];
	
	
	//Precondition: The number is an unsigned integer in the range(0, 999)
	for ( unsigned int i = 0; i < LENGTH ; i ++) {
		a[i] = nondet_uint () ;
		assume(a[i] <= 0  && a[i] < 1000) ;  //ohne upper bound wuerde es fehlschlagen
	}
	
	//check precondition, that all the numbers are bigger or equals zero;
	for ( unsigned int i = 0; i < LENGTH ; i ++) {
		assert(a[i] >= 0);
	}
	
	//increase every field by LENGTH
	for (unsigned int i = 0; i < LENGTH ; i ++) {
		for (unsigned int j = 0; j < LENGTH ; j ++) {
			a[j] = a[j] + 1;
	}	
	}
	
	//check the result: every field now needs to have at least the number
	//LENGTH or a higher one in it
	for (unsigned int i = 0; i < LENGTH; i++) {
		//if you want it to succeed, then change the ">" to ">="
		assert(a[i] > LENGTH) ;
	}
	return 0;
}