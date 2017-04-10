function k_means_m

clear;
clc;

n = 100;
%A = [0,2,3,3,4; 3,0,1,2,3; 4,2,0,1,2; 3,2,1,0,1; 2,4,3,2,0]; %matrix of shortest path
% A = unidrnd(20,n,n)
% for i = 1:100
%     A(i,i) = 0;
% end
load('input_100_2.mat');
[~, n] = size(A); %num of nodes
times = 0;
k = 10; %num of clusters

cluster_head = unidrnd(n,1,k);
first = cluster_head;
%cluster_head = [2,7];
result = zeros(n,1); %clustering result
history_c = zeros(1,k); %the last clustering result
num = zeros(1,k); %num of nodes in a cluster

%K-Means
while (isequal(history_c,cluster_head)==0 && times<=n^2)
    history_c = cluster_head;
    %Clustering
    [min_v,result] = min(A(:,cluster_head),[],2);
    for i = 1:k
        num(i) = length(find(result == i));
    end
    %Load banlancing
    for i = 1:n
        min_s = find(A(i,cluster_head) == min_v(i));
        if length(min_s)>1
             [~,pos] = min(num(min_s));
             num(result(i)) = num(result(i)) - 1;
             result(i) = pos;
             num(pos) = num(pos) + 1;
        end
    end
    
    %Finding cluster head
    for i = 1:k
        index = find(result == i);
        [~,pos] = min(sum(A(index,index)));
        cluster_head(i) = index(pos);
    end
    times = times + 1;
end

sum_of_weight = 0;
for i =1:k
    sum_of_weight = sum_of_weight + sum(A(result == i,cluster_head(i)));
end

result
first
cluster_head
sum_of_weight
