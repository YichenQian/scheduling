clear;
clc;
round = 0;
rounds = 20;
m_r = [];
n_r = [];
o_r = [];
n = 100;
k = 4;
load('input_100_m1.mat');
load('input_100_m2.mat');
num = zeros(1,k); %num of nodes in a cluster

while round < rounds
    round
    black_node = randperm(100);
    black_node = black_node(1:70);
%     while (length(unique(black_node)) ~= length(black_node))
%         black_node = unidrnd(n,1,20);
%     end
    if round < rounds/2
        [m_r,ch,rch] = a_k_means_m(black_node, A1, k)
        if (isequal(ch,rch) == 0)
            %our method
            A1' = A1;
            A1'(ch(1),:) = [];
            A1'(:,ch(1)) = [];
            %Find the nearest black node
            temp = A1'(cluster_head,black_node);
            [min_v,f_bch] = min(temp,[],2);
            while (length(f_bch) ~= length(unique(f_bch)))
                [mm,nn] = hist(f_bch,unique(f_bch));
                b_arrange = nn(mm>1);
                for i = 1 : length(b_arrange)
                    w_arrange = temp(:,b_arrange(i)) == min_v;
                    [~,pos] = min(temp(w_arrange,b_arrange(i)));
                    pp = find(w_arrange == 1);
                    pos = pp(pos);
                    f_bch(pos) = b_arrange(i);
                    w_arrange(pos) = 0;
                    temp(w_arrange,b_arrange(i)) = inf;
                    [~,f_bch(w_arrange)] = min(temp(w_arrange,:),[],2);
                end
            end
            f_bch = black_node(f_bch);
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
            m_r' = sum_of_weight;
            
            %intuitive method
            
        end
        m_r = [m_r, m_r']
        
        
    else
        [m_r,ch,rch] = [m_r, a_k_means_m(black_node, A2, k)]
        
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

