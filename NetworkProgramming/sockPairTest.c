/*
 * =====================================================================================
 *
 *       Filename:  sockPairTest.c
 *
 *    Description:  
 *
 *
 *        Version:  1.0
 *        Created:  2014年10月17日 19时01分55秒
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  YOUR NAME (), 
 *   Organization:  
 *
 * =====================================================================================
 */

#include <sys/types.h>
#include <sys/socket.h>

#include <stdlib.h>
#include <stdio.h>

int Socketpair(int, int, int, int[]);

int main ()
{
	int fds[2];

	int r = Socketpair(AF_INET, SOCK_STREAM, 0, fds);
	if (r < 0) {
		perror( "socketpair()" );
		exit( 1 );
	}

	if(fork()) {
		/*  Parent process: echo client */
		int val = 0;
		close( fds[1] );
		while ( 1 ) {
			sleep(1);
			++val;
			printf( "Sending data: %d\n", val );
			write( fds[0], &val, sizeof(val) );
			read( fds[0], &val, sizeof(val) );
			printf( "Data received: %d\n", val );
		}
	}
	else {
		/*  Child process: echo server */
		int val;
		close( fds[0] );
		while ( 1 ) {
			read( fds[1], &val, sizeof(val) );
			++val;
			write( fds[1], &val, sizeof(val) );
		}
	}
}
