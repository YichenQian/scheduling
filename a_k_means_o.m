function [min_sum] = a_k_means_o(black_node, A, k)
%the optimal result by enumeration;

[~, n] = size(A); %num of nodes
rounds = k*5;
round = 0;
min_sum = inf;
min_result = [];
f_ch = [];
nn = length(black_node);

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
% black_node = unidrnd(n,1,20);
% black_node = [1:5:100];

A = A + A';

for ii = 1 : nn - 4
    for jj = ii + 1 : nn - 3
        for kk = jj + 1 : nn - 2
            for ll = kk +1 :nn - 1
                for mm = kk +1 :nn
                    cluster_head = [black_node(ii), black_node(jj), black_node(kk), black_node(ll), black_node(mm)];
                    %     while (length(unique(cluster_head)) ~= length(cluster_head))
                    %         cluster_head = unidrnd(n,1,k);
                    %     end
                    num = zeros(1,k); %num of nodes in a cluster
                    
                    %Clustering
                    [min_v,result] = min(A(:,cluster_head),[],2);
                    for i = 1:k
                        num(i) = length(find(result == i));
                    end
                    %Load banlancing
                    %                 for i = 1:n
                    %                     min_s = find(A(i,cluster_head) == min_v(i));
                    %                     if length(min_s)>1
                    %                         [~,pos] = min(num(min_s));
                    %                         num(result(i)) = num(result(i)) - 1;
                    %                         result(i) = pos;
                    %                         num(pos) = num(pos) + 1;
                    %                     end
                    %                 end
                    
                    
                    %     %Find the nearest black node
                    %     temp = A(cluster_head,black_node);
                    %     [min_v,f_bch] = min(temp,[],2);
                    %     while (length(f_bch) ~= length(unique(f_bch)))
                    %         [m,n] = hist(f_bch,unique(f_bch));
                    %         b_arrange = n(m>1);
                    %         for i = 1 : length(b_arrange)
                    %             w_arrange = min_v == b_arrange(i);
                    %             [~,pos] = min(temp(w_arrange,b_arrange(i)));
                    %             f_bch(w_arrange(pos)) = b_arrange(i);
                    %             w_arrange(pos) = [];
                    %             temp(w_arrange,b_arrange(i)) = inf;
                    %             [~,f_bch(w_arrange)] = min(temp(w_arrange,:),[],2);
                    %         end
                    %     end
                    %
                    %     %Clustering
                    %     [min_v,result] = min(A(:,f_bch),[],2);
                    %     for i = 1:k
                    %         num(i) = length(find(result == i));
                    %     end
                    %     %Load banlancing
                    %     for i = 1:n
                    %         min_s = find(A(i,f_bch) == min_v(i));
                    %         if length(min_s)>1
                    %             [~,pos] = min(num(min_s));
                    %             num(result(i)) = num(result(i)) - 1;
                    %             result(i) = pos;
                    %             num(pos) = num(pos) + 1;
                    %         end
                    %     end
                    
                    sum_of_weight = 0;
                    for i =1:k
                        sum_of_weight = sum_of_weight + sum(A(result == i,cluster_head(i)));
                    end
                    
                    if sum_of_weight < min_sum
                        min_result = result;
                        f_ch = cluster_head;
                        min_sum = sum_of_weight;
                    end
                end
            end
        end
    end
end
end
