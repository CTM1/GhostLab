#!/usr/bin/env sh

for i in $(seq 1 300); do
	(make testlab || (break && echo "FAIL"))&
done
