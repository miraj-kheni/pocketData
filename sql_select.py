#usage: python3 sql.py filename

import matplotlib.pyplot as plt
import numpy as np
import sys

filename = sys.argv[1]

cpu_frequency = [[],[],[],[]]
cpu_frequency_timestamps = [[],[],[],[]]
c_switch = 0
kmem_alloc = 0
kmem_cache_alloc = 0
sched_switches = []
kmem_allocated = []
kmem_cache_allocated = []
kmem = {}
kmem_cache = {}
kmem_timestamps = []
kmem_cache_timestamps = []
sched_timestamps = []
block_insert = 0
block_complete = 0
block_inserted = []
block_insert_timestamps = []
block_completed = []
block_complete_timestamps = []
first = True

with open(filename,'r') as log:
	for line in log:
		if('START: App' in line):
			columns = line.split()
			pid = columns[0].split('-')[1]
		elif('SELECT_START' in line):
			line = line.strip()
			columns = line.split()
			start_time = columns[3][:-1]
		elif('SELECT_END' in line):
			line = line.strip()
			columns = line.split()
			end_time = columns[3][:-1]
			break

with open(filename,'r') as log:
	while(not(start_time in log.readline())):
		pass
	line = log.readline()
	while(not(end_time in line)):
		if(pid in line and 'sched_switch' in line):
			line = line.strip()
			columns = line.split()
			if(first == True):
				cpuid = columns[2][1:-2]
				start = float(columns[3][:-1])
				first = False
			c_switch += 1
			sched_timestamps.append(float(columns[3][:-1])*1000)
			sched_switches.append(c_switch)
		'''elif(pid in line and 'kmalloc' in line):
			line = line.strip()
			columns = line.split()	
			if(first == True):
				cpuid = columns[2][1:-2]
				start = float(columns[3][:-1])
				first = False
			kmem_alloc += float(columns[8][12:])
			kmem[columns[6][4:]] = float(columns[8][12:])
			kmem_timestamps.append(float(columns[3][:-1]))
			kmem_allocated.append(kmem_alloc)
		elif(('ple.SqliteTrace' in line or '...' in line) and 'kfree' in line):
			line = line.strip()
			columns = line.split()	
			if(first == True):
				cpuid = columns[2][1:-2]
				start = float(columns[3][:-1])	
				first = False
			if(columns[6][4:] in kmem):
				kmem_alloc -= float(kmem[columns[6][4:]])
				kmem_timestamps.append(float(columns[3][:-1]))
				kmem_allocated.append(kmem_alloc)
		elif(('Sqlite' in line or '...' in line) and 'kmem_cache_alloc' in line):
			line = line.strip()
			columns = line.split()
			if(first == True):
				cpuid = columns[2][1:-2]
				start = float(columns[3][:-1])
				first = False
			kmem_cache_alloc += float(columns[8][12:])
			kmem_cache[columns[6][4:]] = float(columns[8][12:])
			kmem_cache_timestamps.append(float(columns[3][:-1]))
			kmem_cache_allocated.append(kmem_cache_alloc)
		elif(('Sqlite' in line or '...' in line) and 'kmem_cache_free' in line):
			line = line.strip()
			columns = line.split()
			if(first == True):
				cpuid = columns[2][1:-2]
				start = float(columns[3][:-1])
				first = False
			if(columns[6][4:] in kmem_cache):
				kmem_cache_alloc -= float(kmem_cache[columns[6][4:]])
				kmem_cache_timestamps.append(float(columns[3][:-1]))
				kmem_cache_allocated.append(kmem_cache_alloc)'''
		if(pid in line and 'block_rq_insert' in line):
			line = line.strip()
			columns = line.split()
			if(first == True):
				cpuid = columns[2][1:-2]
				start = float(columns[3][:-1])
				first = False
			block_insert += 1
			block_inserted.append(block_insert)
			block_insert_timestamps.append(float(columns[3][:-1])*1000)
		elif('block_rq_complete' in line):
			line = line.strip()
			columns = line.split()
			if(first == True):
				cpuid = columns[2][1:-2]
				start = float(columns[3][:-1])
				first = False
			block_complete += 1
			block_completed.append(block_complete)
			block_complete_timestamps.append(float(columns[3][:-1])*1000)

		line = log.readline()
	end=float(line.split()[3][:-1])


with open(filename,'r') as log:
	for line in log:
		if('cpu_frequency_switch_start' in line):
			line = line.strip()
			columns = line.split()
			cpu = int(columns[7][-1])
			cpu_frequency[cpu].append(float(columns[6][4:])/10**5)
			cpu_frequency_timestamps[cpu].append(float(columns[3][:-1]))
		elif('cpu_frequency_switch_end' in line):
			line = line.strip()
			columns = line.split()
			cpu = int(columns[5][-1])
			cpu_frequency[cpu].append(cpu_frequency[cpu][-1])
			cpu_frequency_timestamps[cpu].append(float(columns[3][:-1]))

fig, (ax1, ax2, ax3, ax4) = plt.subplots(4,1,sharex = True)

sched_timestamps = np.array(sched_timestamps) - float(start_time)*1000
#kmem_timestamps = np.array(kmem_timestamps) - float(start_time)
#kmem_cache_timestamps = np.array(kmem_cache_timestamps) - float(start_time)
print(c_switch,block_insert,block_complete)
block_insert_timestamps = np.array(block_insert_timestamps) - float(start_time)*1000
block_complete_timestamps = np.array(block_complete_timestamps) - float(start_time)*1000
x_range = [0,sched_timestamps[-1]]
plt.xlim(x_range)
ax1.set_title('Context Switches1')
ax1.plot(sched_timestamps,sched_switches)
#ax2.set_title('kmem utilization')		
#ax2.plot(kmem_timestamps,kmem_allocated)
#ax3.set_title('kmem_cache utilization')
#ax3.plot(kmem_cache_timestamps,kmem_cache_allocated)
ax4.set_title('block operations1')
ax4.plot(block_insert_timestamps,block_inserted)
ax4.plot(block_complete_timestamps,block_completed)
#ax2.plot(kmem_free_timestamps, kmem_freed)
#frame = plt.gca()
#frame.axes.get_xaxis().set_ticks([])

plt.show()

