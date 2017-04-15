round = 0;
rounds = 1;
m_r = [];
n_r = [];
n = 100;
k = 2;
load('input_100_m2.mat');

while round < rounds
%    cluster_head = unidrnd(n,1,k);
%    while (length(unique(cluster_head)) ~= length(cluster_head))
%        cluster_head = unidrnd(n,1,k);
%    end
    m_r = [m_r, a_k_means_m(cluster_head,A)];
    n_r = [n_r, a_k_means_n(cluster_head,A)];
    round = round + 1;
end

x = 1:rounds;
figure(1);
plot(x,m_r,'-r.',x,n_r,':k.');

figure(2);
plot(x,sort(m_r),'-r.',x,sort(n_r),':k.');

