%����������Բ�������������������ͼ�Լ���Ӧ�����Ӿ� %����Ҫ����Ĳ����ǵ���num
function [L]=connectionM(num)
%��������A������ű�ʾ����֮��Ȩֵ�ľ���A��Ҳ�����ٽӾ�����ô����֮��Ȩֵ��Ϊ��Ԫ�صĸ�����Ϊ�õ�Ķ���
DEF=num; %�趨һ������ ����ı������ĸ���
L=rand(DEF);%����DEF*DEF���������
for i=1:DEF
    L(i,i)=0;%���Խ����ϵ�����Ϊ0
end
L=10*L;
L=floor(L);%����ȥ��
L=mod(L,2);
for i=1:DEF
    for j=1:i
        L(j,i)=L(i,j);%��A�����Ϊһ�������ǻ��������Ǿ���
    end
end
x=100*rand(1,DEF);y=100*rand(1,DEF);%����10������ĵ�
plot(x,y,'r+');
for i=1:DEF
    a=find(L(i,:)>0);%��A����ÿ�д���0�������ڸ��еĵ�ַ�ҳ�������a��
    for j=1:length(a)
        c=num2str(L(i,j)); %��A�е�Ȩֵת��Ϊ�ַ���
        hold on;
        line([x(i) x(a(j))],[y(i) y(a(j))]);%����
    end
end
title('�������ͼ');
e=num2str(DEF);
legend(e);%���Ͻ���ʾ�ڵ�ĸ���
for m=1:DEF
    f=num2str(m);
    hold on;
    text((x(m)+x(m))/2,(y(m)+y(m))/2,f,'Fontsize',18); %��Ȩֵ��ʾ�����������м�
end
hold off