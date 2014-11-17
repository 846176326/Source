/*************************************************************************
  > File Name: send_recv.c
  > Author: xuyue
  > Mail: xuyue0531@gmail.com 
  > Created Time: 2014年08月18日 星期一 20时32分02秒
 ************************************************************************/


#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<netinet/ip_icmp.h>
#include<netinet/tcp.h>
#include<netinet/udp.h>
#include<arpa/inet.h>
#include<sys/socket.h>
#include<sys/types.h>
#include<linux/if_ether.h>
#include<arpa/inet.h>
#include<unistd.h>
#include<pthread.h>

#include"common.h"



int main(int argc, int *argv[])
{
	int sockfd, rawsock;
	pthread_t tid;
	unsigned char buff[BUFFSIZE];
	int n, slen, port;
	int count = 0;
	char ch;
	struct sockaddr_in servaddr;
	Filter *filter;

	filter = (struct Filter*)malloc(sizeof(struct Filter));
	rawsock = socket(PF_PACKET,SOCK_DGRAM, htons(ETH_P_IP));
	if(rawsock < 0) {
		printf("raw socket error!\n");
		exit(1);
	}
	filter->sockfd = rawsock;

	while((ch = getopt(argc, argv, "p:s:d:h:o")) != -1) {
		slen = strlen(optarg);
		switch (ch) {
			case 'p':
				if(slen > 4){
					fprintf(stdout, "The protocol is error!\n");
					return -1;
				}
				memcpy(filter->proto, optarg, slen);
				filter->proto[slen] = '\0';
				break;
			case 's':
				if(slen > 15 || slen < 7){
					fprintf(stdout, "The IP address is error!\n");
					return -1;
				}
				memcpy(filter->saddr, optarg, slen);
				filter->saddr[slen] = '\0';
				break;
			case 'd':
				if(slen > 15 || slen < 7){
					fprintf(stdout, "The IP address is error!\n");
					return -1;
				}
				memcpy(filter->daddr, optarg, slen);
				filter->saddr[slen] = '\0';
				break;
			case 'o':
				port = atoi(optarg);
			case 'h':
				fprintf(stdout, "usage: snffer [-p protocol] [-s source_ip_address] [-d dest_ip_address]\n"
						"	-p	protocol[TCP/UDP/ICMP]\n"
						"	-s	souce ip address\n"
						"	-d	dest ip address\n");
				exit(0);
			case '?':
				fprintf(stdout, "unrecongized option: %c\n", ch);
				exit(-1);
		}
	}

	pthread_create(&tid, NULL, &recvMsg, filter);

	sockfd = socket(AF_INET, SOCK_DGRAM, 0);
	bzero(&servaddr, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_port = htons(port);
	inet_pton(AF_INET, (sockaddr*)(&servaddr), sizeof(servaddr));

	while(1) {

		sendto(sockfd, sndmsg, strlen(sndmsg), 0, servaddr, sizeof(servaddr));
	}


}


void
recvMsg(Filter *filter) 
{	
	int n, count;
	unsigned char *buff;
	int rawsock = filter->sockfd;

	pthread_detach(pthread_self());
	buff = (unsigned char*)malloc(BUFFSIZE);

	while(1) {	
		n = recvfrom(rawsock, buff, BUFFSIZE, 0, NULL, NULL);
		if(n<0){
			printf("receive error!\n");
			exit(1);
		}

		count++;
		struct ip *ip = (struct ip*)(buff);
		if(strlen(filter->proto)){
			if(!strcmp(filter->proto, "TCP")){
				if(ip->ip_p != IPPROTO_TCP)
					continue;
				else 
					goto addr;
			}else if(!strcmp(filter->proto, "UDP")){
				if(ip->ip_p != IPPROTO_UDP)
					continue;
				else 
					goto addr;
			}else if(!strcmp(filter->proto, "ICMP")){
				if(ip->ip_p != IPPROTO_ICMP)
					continue;
				else
					goto addr;
			}
		}

addr:
		if(strlen(filter->saddr)){
			strcpy(filter->address, inet_ntoa(ip->ip_src));
			if(strcmp(address, filter->saddr) != 0)
				continue;
		}
		if(strlen(filter->daddr)){
			strcpy(filter->address, inet_ntoa(ip->ip_dst));
			if(strcmp(filter->address, filter->daddr) != 0)
				continue;
		}
	}

	
	printf("%4d	%15s",count,inet_ntoa(ip->ip_src));
	printf("%15s	%5d	%5d\n",inet_ntoa(ip->ip_dst),ip->ip_p,ntohs(ip->ip_len));

	printDate(buff);
}


void
printDate(void *arg) 
{	
	char *buff = (char*)arg; 
	int i=0,j=0;
	for(i=0;i<n;i++){
		if(i!=0 && i%16==0){
			printf("	");
			for(j=i-16;j<i;j++){
				if(buff[j]>=32&&buff[j]<=128)
					printf("%c",buff[j]);
				else printf(".");
			}
			printf("\n");
		}
		if(i%16 == 0) printf("%04x	",i);			
		printf("%02x",buff[i]);

		if(i==n-1){
			for(j=0;j<15-i%16;j++) printf("  ");
			printf("	");
			for(j=i-i%16;j<=i;j++){
				if(buff[j]>=32&&buff[j]<127)
					printf("%c",buff[j]);
				else printf(".");

			}
		}
	}
}















