# You can uncomment the following lines to produce a png figure
set terminal png enhanced
set output 'plot_star_avg.png'

set title "Average Shortest Path Length"
set xlabel "cycles"
set ylabel "average path length (log)"
set key right top
set logscale y 
plot "path_len_star_c30" title 'Basic Shuffle c = 30' with lines, \
	"path_len_random_c30" title 'Random Graph c = 30' with lines, \
	"path_len_star_c50" title 'Basic Shuffle c = 50' with lines, \
	"path_len_random_c50" title 'Random Graph c = 50' with lines