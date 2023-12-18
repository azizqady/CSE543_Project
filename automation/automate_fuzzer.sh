#!/bin/bash

iteration=1
counter=0

while true; do
    java Fuzzer -s seed_file $iteration -o prog_no.crash; /challenge/prog_no < prog_no.crash; echo "The iteration is: $iteration"
   
    counter=$((counter + 1))

    if [[ $counter -eq 100 ]]; then
        clear
        counter=0
    fi

    iteration=$((iteration + 1))
done

# Clean up temporary file
rm output.txt
