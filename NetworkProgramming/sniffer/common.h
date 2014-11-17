/*************************************************************************
    > File Name: common.h
    > Author: xuyue
    > Mail: xuyue0531@gmail.com 
    > Created Time: 2014年08月18日 星期一 20时50分04秒
 ************************************************************************/

#ifndef COMMON_H
#define COMMON_H

#define BUFFSIZE 1024

typedef struct {
	int sockfd;
	char proto[6],
		 saddr[20] = {},
		 daddr[20] ={},
		 address[20]= {};
}Filter;


#endif
