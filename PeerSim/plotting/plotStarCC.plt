# You can uncomment the following lines to produce a png figure
set terminal png enhanced
set output 'plot_star_cc.png'

set title "Average Clustering Coefficient"
set xlabel "cycles"
set ylabel "clustering coefficient (log)"
set key right top
set logscale y 
plot "clustering_star_c30" title 'Basic Shuffle c = 30' with lines, \
	"clustering_random_c30" title 'Random Graph c = 30' with lines, \
	"clustering_star_c50" title 'Basic Shuffle c = 50' with lines, \
	"clustering_random_c50" title 'Random Graph c = 50' with lines