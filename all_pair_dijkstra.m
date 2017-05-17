function [A]=all_pair_dijkstra(A)
[n, ~] = size(A);
for i = 1:n
    for j = 1:n
        [A(i,j),~] = dijkstra(A,i,j);
    end
end