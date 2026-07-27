# Gaalop
Gaalop (Geometic Algebra Algorithms Optimizer) is a software to compile and optimize geometric algebra (GA) expressions into high-level programming language code. Geometric algebra expressions can be developed using the freely available CLUCalc software by Christian Perwass. Gaalop optimizes CLUCalc expressions and produces C++ (AMP), OpenCL, CUDA, CLUCalc or LaTeX output. The optimized code is free of geometric algebra operations and runs very efficiently on various platforms.

<img width="712" height="161" alt="HeapView" src="https://github.com/user-attachments/assets/b774d748-ecf1-4001-8e3f-4e579aafc52a" />

# Whats new in this fork
- UI to visualize product tables
- Some bug fixes in the context of precalculation tables/algebra definition reader from file
- UI to visualize heap-size, max heap-size  (textual/graphically) and an action to execute garbage collection
- Extention of the algebra-definition which allows to use names which do not start with "e" (Useful in the context of quantum computing simulation)
- Extention of multiplication tables (inclusive its precomputation file formats) which allows to work with entries of arbitrary floating point numbers (limitation to 0, -1, 1 only for the squared basis vector values)
- Bugfixes in the Latex code-generator and a new codegenerator to create Maxima-Input for further processing of the gaalop symbolic expressions (e.g. using zero-finding functionality of Maxima to find robot singularities)
- Generic algebras for quantum computing (quantum register algebra - qra and quantum computing algebra - qca) added in a generic way for arbitrary number of qubits. In the UI the user can specify the dimension/qubits.
- Additional Wiki pages
  
# What is Geometric Algebra?
A good explaination can be found [here](https://slehar.wordpress.com/2014/03/18/clifford-algebra-a-visual-introduction/) in the Web
or look into this [book](http://www.amazon.de/Foundations-Geometric-Algebra-Computing-Geometry/dp/3642317936) from Dietmar Hildenbrand.

# Gaalop Precompiler (GPC)
Note that this repository also contains Gaalop Precompiler (GPC)
in the branch gaalop_precompiler.
GPC integrates Gaalop directly into CMake-generated C/C++-toolchains.
Gaalop Precompiler reuses most of the code of Gaalop
which is why merges from Gaalop are required quite often.

Be careful: This part of the codebase is not maintained at the moment. Especially merges from the main branch are not done.

# License
The code of both projects is licensed under the LGPL 3.0.

# Contributions
Contributions are welcome.
Please fork and create a pull request.

# How to compile?
Please visit the [wiki](https://github.com/orat/Gaalop/wiki) on this repository. 
You will find manuals for compiling GAALOP and GAALOP Precompiler.
