Here is a clean, professional README for your project.

---

# SkParser

A high-performance, graph-based command and expression parser designed as a faster alternative to the standard Skript parsing engine.

## Overview

SkParser replaces the traditional linear-scan parsing method `O(N=possible patterns)` with a predictive graph traversal `O(D=command depth)`. This decouples parsing performance from the number of registered syntax patterns, allowing for an unlimited number of commands and expressions without performance degradation.

## Features

* **Graph-Based Execution:** minimal overhead; parses based on sentence depth rather than total syntax count.
* **Ambiguity Resolution:** Handles overlapping syntax using greedy matching and type-based disambiguation.
* **Annotation-Based Registration:** Automatically injects parsed arguments into Java fields.

## Performance Note

Unlike legacy parsers that iterate through every possible pattern for every line of code, SkParser jumps directly to the relevant command node. This architecture ensures that adding new features or addons does not slow down the parsing engine.