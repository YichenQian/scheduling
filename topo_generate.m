function [AD, A]=topo_generate(num)
%initialization
max_weight = 10;
AD = inf(num, num);
for i = 1:num
	AD(i, i) = 0;
end
%connections
for i = 1 : num
	con_num = floor(num / 20);  %the max number of the connections on node i
	l_num  = unidrnd(con_num);  %the number of the connections on node i
	l = randperm(5);
    l = l(1 : l_num);  %the nodes that connected to the node i (1-5)
    for j = 1 : l_num
        m = mod(i + l(j) - 1, num) + 1;
        AD(i, m) = unidrnd(max_weight);
        AD(m, i) = unidrnd(max_weight);  %update the adjust matrix according to the connections
    end
end

A = all_pair_dijkstra(AD);

%plot
x = 100 * rand(1, num);
y = 100 * rand(1, num);
plot(x, y, 'r+-');
for i = 1 : num
    a = find(AD(i, :) < max_weight+1);
    for j = 1 : length(a)
        c = num2str(AD(i, j));
        hold on;
        line([x(i) x(a(j))],[y(i) y(a(j))])
    end
end
title('Random topology graph');
e=num2str(num);
legend(e);
for m = 1 : num
    f=num2str(m);
    hold on;
    text((x(m) + x(m)) / 2, (y(m) + y(m)) / 2, f, 'Fontsize', 18);
end
hold off