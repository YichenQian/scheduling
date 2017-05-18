function [min_sum] = mcl(black_node, AD, k)

%initialization
e = 2;
r = 2;
his  = zeros(n,n);

%matrix initialzation
[n,~] = size(AD);
ADM = AD;
ADM(ADM == inf) = 0;
maxa = max(max(ADM))
ADM = maxa + 1 - ADM;
ADM(ADM == maxa + 1) = 0;
for i = 1 : n
    ADM(:, i) = ADM(:, i) / sum(ADM(:, i));
end

%begin

ADM = ADM^e;
ADM = ADM.^2;
for i = 1 : n
    ADM(:, i) = ADM(:, i) / sum(ADM(:, i));
end