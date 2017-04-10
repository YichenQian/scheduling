%这个函数可以产生任意点数的随机拓扑图以及对应的连接矩 %阵，需要输入的参数是点数num
function [L]=connectionM(num)
%产生数组A用来存放表示两点之间权值的矩阵A，也就是临接矩阵，那么两点之间权值不为零元素的个数即为该点的度数
DEF=num; %设定一个东东 方便改变随机点的个数
L=rand(DEF);%产生DEF*DEF的随机矩阵
for i=1:DEF
    L(i,i)=0;%将对角线上的数置为0
end
L=10*L;
L=floor(L);%向下去整
L=mod(L,2);
for i=1:DEF
    for j=1:i
        L(j,i)=L(i,j);%将A矩阵变为一个上三角或者下三角矩阵
    end
end
x=100*rand(1,DEF);y=100*rand(1,DEF);%产生10个随机的点
plot(x,y,'r+');
for i=1:DEF
    a=find(L(i,:)>0);%将A矩阵每行大于0的数的在该行的地址找出来放在a中
    for j=1:length(a)
        c=num2str(L(i,j)); %将A中的权值转化为字符型
        hold on;
        line([x(i) x(a(j))],[y(i) y(a(j))]);%连线
    end
end
title('随机拓扑图');
e=num2str(DEF);
legend(e);%左上角显示节点的个数
for m=1:DEF
    f=num2str(m);
    hold on;
    text((x(m)+x(m))/2,(y(m)+y(m))/2,f,'Fontsize',18); %将权值显示在两点连线中间
end
hold off