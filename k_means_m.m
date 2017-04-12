function k_means_m
%choose the black node after k-means

clear;
clc;

n = 100;
%A = [0,2,3,3,4; 3,0,1,2,3; 4,2,0,1,2; 3,2,1,0,1; 2,4,3,2,0]; %matrix of shortest path
% A = unidrnd(20,n,n)
% for i = 1:100
%     A(i,i) = 0;
% end
load('input_100_m2.mat');
[~, n] = size(A); %num of nodes
times = 0;
k = 2; %num of clusters
rounds = k*5;
round = 0;
min_sum = inf;
min_result = [];
f_ch = [];

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%random generate the graph
% A = inf(n,n);
% load('temp_L.mat');
% for i = 1:n
%     for j = 1:n
%         if L(i,j) == 1;
%             A(i,j) = unidrnd(20);
%         end
%     end
%     A(i,i) = 0;
% end
%
% for i = 1:n
%     for j = 1:n
%         [A(i,j),~] = dijkstra(A,i,j);
%     end
% end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% for i = 2:50
%     A(i,1) = 1;
%     A(1,i) = 1;
% end
%
% for i = 52:100
%     A(i,51) = 1;
%     A(51,i) = 1;
% end

%random generate the graph
% A = inf(n,n);
% for i = 1:n
%     connect = unidrnd(n,1,5);
%     for j = 1:length(connect)
%         A(i,connect(j)) = unidrnd(20);
%         A(connect(j),i) = unidrnd(20);
%     end
%     A(i,i) = 0;
% end

% for i = 1:n
%     for j = 1:n
%         [A(i,j),~] = dijkstra(A,i,j);
%     end
% end

%random generate black node
black_node = unidrnd(n,1,20);
black_node = [1:5:100];

while (round < rounds)
    cluster_head = unidrnd(n,1,k);
    while (length(unique(cluster_head)) ~= length(cluster_head))
        cluster_head = unidrnd(n,1,k);
    end
    first = cluster_head;
    cluster_head = [2,60];
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
    
    %Find the nearest black node
    temp = A(cluster_head,black_node);
    [min_v,f_bch] = min(temp,[],2);
    while (length(f_bch) ~= length(unique(f_bch)))
        [m,n] = hist(f_bch,unique(f_bch));
        b_arrange = n(m>1);
        for i = 1 : length(b_arrange)
            w_arrange = min_v == b_arrange(i);
            [~,pos] = min(temp(w_arrange,b_arrange(i)));
            f_bch(w_arrange(pos)) = b_arrange(i);
            w_arrange(pos) = [];
            temp(w_arrange,b_arrange(i)) = inf;
            [~,f_bch(w_arrange)] = min(temp(w_arrange,:),[],2);
        end
    end
    
    %Clustering
    [min_v,result] = min(A(:,f_bch),[],2);
    for i = 1:k
        num(i) = length(find(result == i));
    end
    %Load banlancing
    for i = 1:n
        min_s = find(A(i,f_bch) == min_v(i));
        if length(min_s)>1
            [~,pos] = min(num(min_s));
            num(result(i)) = num(result(i)) - 1;
            result(i) = pos;
            num(pos) = num(pos) + 1;
        end
    end
    
    sum_of_weight = 0;
    for i =1:k
        sum_of_weight = sum_of_weight + sum(A(result == i,f_bch(i)));
    end
    
    if sum_of_weight < min_sum
        min_result = result;
        f_ch = f_bch;
        min_sum = sum_of_weight;
    end
    
    round = round + 1;
end

min_result'
first
f_ch
min_sum
