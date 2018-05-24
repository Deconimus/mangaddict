import os, sys, subprocess


path = os.path.dirname(os.path.abspath(__file__)).replace("\\", "/")


def main():
	
	if not os.path.exists(path+"/make.json"):
		print("\""+path+"/make.json\" not found.")
		return
	
	jarMake = ""
	
	for p in os.environ["PATH"].split(";"):
		
		if p.lower().endswith("jarmake"):
			jarMake = p+"/jarMake.py"
			
	if (not os.path.exists(jarMake)) and os.path.exists(path+"/jarMake/jarMake.py"):
		
		jarMake = path+"/jarMake/jarMake.py"
		
	if not os.path.exists(jarMake):
		
		print("jarMake not found.")
		return
		
	jarMake = jarMake.replace("\\", "/")
	
	cmd = ["python", jarMake, path+"/make.json"]
	
	if len(sys.argv) > 1:
		cmd = cmd+sys.argv[1:]
	
	subprocess.call(cmd)
	
	
if __name__ == "__main__":
	
	main()