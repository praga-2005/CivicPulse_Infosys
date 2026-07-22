import urllib.request
import json

req = urllib.request.Request("http://localhost:8080/api/citizens/login", 
    data=b'{"email":"pragathi@gmail.com","password":"123456"}',
    headers={"Content-Type": "application/json"})

try:
    with urllib.request.urlopen(req) as response:
        res = json.loads(response.read().decode())
        token = res["token"]
        print("Got token:", token)
        
        citizen_id = res["citizen"]["id"]
        print("Citizen ID:", citizen_id)
        
        req2 = urllib.request.Request(f"http://localhost:8080/api/grievances?citizenId={citizen_id}",
            headers={"Authorization": "Bearer " + token, "Origin": "http://localhost:5176"})
        
        try:
            with urllib.request.urlopen(req2) as r2:
                print("Grievances response:", r2.status, r2.read().decode())
        except urllib.error.HTTPError as e:
            print("Grievances HTTPError:", e.code)
            print("Headers:", e.headers)
            print("Body:", e.read().decode())
except urllib.error.HTTPError as e:
    print("Login HTTPError:", e.code, e.read().decode())
