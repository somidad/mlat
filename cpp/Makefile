CXX=g++
STD=-std=c++11

obj = mlat.o

demo: $(obj) demo.cc
	$(CXX) $(STD) mlat.o demo.cc -o demo

$(obj): mlat.cc
	$(CXX) $(STD) -c mlat.cc

clean:
	rm -f demo mlat.h.gch mlat.o

.PHONY: clean

