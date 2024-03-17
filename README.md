# Scheduler Project

## Overview

The Scheduler project is designed to simulate task scheduling in a system with four processors. It consists of a main thread responsible for printing program output and a separate thread managing four processors. The project implements Round Robin scheduling algorithm. Tasks are placed in ready and waiting queues based on priority, and processors execute tasks from the ready queue.

## Classes

### 1. MainThread

Responsible for printing program output. Displays information about the currently executing task and the processor handling it.

### 2. Processor

Handles task execution, choosing tasks from the ready queue, and transitioning between running and idle states.

### 3. Resource

Stores the number and names of resources in the scheduler.

### 4. Task

Represents a task with attributes like duration, type, state, and name. States include Ready, Running, and Waiting. Type of a task represent its priority as follows: Z(highest priority), Y, X(lowest priority)

## Queues

The scheduler includes two main queues:

### 1. Ready Queue

Tasks are placed in this queue based on priority. Processors select tasks from the ready queue for execution.

### 2. Waiting Queue

Tasks move to this queue if there are enough processors but insufficient resources. Tasks are moved from the waiting queue to the ready queue when resources become available.

## Scheduling Algorithms

**1. Round Robin (RR):** Tasks are executed in a circular order, allowing each task to run for a fixed time slice before moving to the next task.
