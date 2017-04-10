function k_means

clear;clc

n = 100; %num of nodes
%A = [0,2,3,3,4; 3,0,1,2,3; 4,2,0,1,2; 3,2,1,0,1; 2,4,3,2,0]; %matrix of shortest path
%A = unidrnd(20,n,n)
load('input_100_2.mat')
[M,N] = size(A); %size of the matrix
times = 0;
k = 2; %num of clusters

cluster_head = unidrnd(M,1,k);
first = cluster_head;
%cluster_head = [1,5];
min = Inf(1,n); %the min distance between a cluster head and a node
result = zeros(1,n); %clustering result
history_c = zeros(1,k); %the last clustering result
num = zeros(1,k); %num of nodes in a cluster
%num_count = zeros(1,k-1);
%num_count = [n, num_count];

%K-Means
while (isequal(history_c,cluster_head)==0 && times<=n^2)
    history_c = cluster_head;
    min = Inf(1,n);
    %Clustering
    for i = 1:n
        for j = 1:k
            if A(i,cluster_head(j)) < min(i)
                result(i) = j;
                min(i) = A(i,cluster_head(j));
                
            elseif A(i,cluster_head(j)) == min(i)
                %consider load banlance and hop count
                
            end
        end
    end
    
    %Finding cluster head
    for i = 1:k
        min_sum_weight = Inf;
        index = find(result == i);
        for j = 1:length(index)
            if sum(A(index,index(j))) < min_sum_weight
                cluster_head(i) = index(j);
                min_sum_weight = sum(A(index,index(j)));
            elseif sum(A(index,index(j))) == min_sum_weight
                %consider hop count
            end
        end
    end
    times = times + 1;
end

sum_of_weight = 0;
for i =1:k
    index = find(result == i);
    sum_of_weight = sum_of_weight + sum(A(index,cluster_head(i)));
end

result
first
cluster_head
sum_of_weight
