clear;
clc;
round = 0;
rounds = 20;
m_r = [];
n_r = [];
o_r = [];
n = 100;
k = 3;
load('input_100_m3.mat');
load('input_100_m4.mat');

while round < rounds
    round
    black_node = randperm(100);
    black_node = black_node(1:70);
%     while (length(unique(black_node)) ~= length(black_node))
%         black_node = unidrnd(n,1,20);
%     end
    if round < rounds/2
        [m_rr,~,~] = a_k_means_m(black_node, A3, k);
        m_r = [m_r, m_rr]
        n_r = [n_r, a_k_means_n(black_node, A3, k)]
        o_r = [o_r, a_k_means_o(black_node, A3, k)]
    else
        [m_rr,~,~] = a_k_means_m(black_node, A4, k);
        m_r = [m_r, m_rr]
        n_r = [n_r, a_k_means_n(black_node, A4, k)]
        o_r = [o_r, a_k_means_o(black_node, A4, k)]
    end
    round = round + 1;
end

x = 1:rounds;

[~,index] = sort(o_r);

figure(1);
plot(x,m_r,'-r.',x,n_r,':k.',x,o_r,'-b.');
title('Algorithm Performance');
xlabel('Different Networks');
ylabel('Total Cost');
legend('Proposed algorithm','Another algorithm','Theoretical optimal value');

figure(2);
plot(x,sort(m_r),'-r.',x,sort(n_r),':k.',x,sort(o_r),'-b.');
title('Algorithm Performance');
xlabel('Different Networks');
ylabel('Total Cost');
legend('Proposed algorithm','Another algorithm','Theoretical optimal value');

figure(3);
plot(x,m_r(index),'-r.',x,n_r(index),':k.',x,o_r(index),'-b.');
title('Algorithm Performance');
xlabel('Different Networks');
ylabel('Total Cost');
legend('Proposed algorithm','Another algorithm','Theoretical optimal value');

