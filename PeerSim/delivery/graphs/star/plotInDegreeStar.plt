set terminal png enhanced
set output 'plot_star_in.png'

set title "In-degree distribution (Star)"
set xlabel "in-degree"
set ylabel "number of nodes"
set key right top
plot "in_deg_star_c30" title 'Basic Shuffle c = 30' with histeps, \
	"in_deg_random_c30" title 'Random Graph c = 30' with histeps, \
	"in_deg_star_c50" title 'Basic Shuffle c = 50' with histeps, \
	"in_deg_random_c50" title 'Random Graph c = 50' with histeps