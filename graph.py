#usage: python3 sql.py filename

import matplotlib.pyplot as plt
import numpy as np

filename = "trace2.log"

cpu_frequency = [[],[],[],[]]
cpu_frequency_timestamps = [[],[],[],[]]
c_switch = 0
sched_timestamps = []
sched_switches = []
cumulative_time = 0
cumul_time = []
block = {}
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
        elif('UPDATE_START' in line):
            line = line.strip()
            columns = line.split()
            start_time = columns[3][:-1]
        elif('UPDATE_END' in line):
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
            if(('next_pid='+pid) in line):
                sched_switches.append(0)
            else:
                sched_switches.append(1)
                
        if(pid in line and 'block_rq_insert' in line):
            line = line.strip()
            columns = line.split()
            if(first == True):
                cpuid = columns[2][1:-2]
                start = float(columns[3][:-1])
                first = False
            block[(str(columns[9])+' +'+str(columns[11]))] = 1
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
            if(str(columns[8]+' +'+str(columns[10])) in block):
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

fig, (ax1, ax2) = plt.subplots(2,1,sharex = True)

sched_timestamps = np.array(sched_timestamps) - float(start_time)*1000
switch_in_time = np.array(switch_in_time) - float(start_time)*1000
switch_out_time = np.array(switch_out_time) - float(start_time)*1000

#kmem_timestamps = np.array(kmem_timestamps) - float(start_time)
#kmem_cache_timestamps = np.array(kmem_cache_timestamps) - float(start_time)
print(c_switch,block_insert,block_complete)
block_insert_timestamps = np.array(block_insert_timestamps) - float(start_time)*1000
block_complete_timestamps = np.array(block_complete_timestamps) - float(start_time)*1000
x_range = [0,float(end_time)*1000 - float(start_time)*1000]
print(float(end_time)*1000 - float(start_time)*1000)
plt.xlim(x_range)
ax1.set_title('Context Switches')
sched_timestamps = np.insert(sched_timestamps,0,0)
sched_switches = np.insert(sched_switches,0,0)
sched_timestamps = np.append(sched_timestamps,float(end_time)*1000 - float(start_time)*1000)
sched_switches = np.append(sched_switches,1)

ax1.step(sched_timestamps,sched_switches,'k',label='')
ax1.set_ylim((0,2))
legends2 = ax1.legend(loc='lower right')
#ax1.set_xlabel('runtime (milliseconds)')
ax1.set_ylabel('number of context switches')

block_insert_timestamps = np.insert(block_insert_timestamps,0,0)
block_inserted = np.insert(block_inserted,0,0)
block_complete_timestamps = np.insert(block_complete_timestamps,0,0)
block_completed = np.insert(block_completed,0,0)

ax2.set_title('block_operations')
ax2.plot(block_insert_timestamps,block_inserted,'o',label='')
ax2.plot(block_insert_timestamps,block_inserted,'k',label='block operation start event')
ax2.plot(block_complete_timestamps,block_completed,'o',label='')
ax2.plot(block_complete_timestamps,block_completed,'k',label='block operation end event')
ax2.set_ylabel('number of block operations')
legends2 = ax2.legend(loc='lower right')

fig.text(0.5, 0.04, 'runtime(in ms)', ha='center', va='center')
#ax2.set_title('kmem utilization')        
#ax2.plot(kmem_timestamps,kmem_allocated)
#ax3.set_title('kmem_cache utilization')
#ax3.plot(kmem_cache_timestamps,kmem_cache_allocated)
#ax4.set_title('block operations1')
#ax4.plot(block_insert_timestamps,block_inserted)
#ax4.plot(block_complete_timestamps,block_completed)
#ax2.plot(kmem_free_timestamps, kmem_freed)
#frame = plt.gca()
#frame.axes.get_xaxis().set_ticks([])

plt.show()

