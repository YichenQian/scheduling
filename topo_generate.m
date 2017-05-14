function [L]=topo_generate(num)
DEF=num;
con_num = floor(num / 20);
L  = rand(con_num)+1;
