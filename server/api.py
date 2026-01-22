from fastapi import FastAPI, Body
import requests
from datetime import datetime
from pydantic import BaseModel

app = FastAPI()

PSE_API_URL = "https://api.raporty.pse.pl/api/rce-pln"

@app.get("/energy/prices")
def get_pse_prices(date: str):
    params = {"$filter": f"business_date eq '{date}'"}
    headers = {'User-Agent': 'Mozilla/5.0', 'Accept': 'application/json'}
    
    try:
        response = requests.get(PSE_API_URL, params=params, headers=headers)
        response.raise_for_status()
        raw_data = response.json()
        
        values = raw_data.get('value', [])
        if not values:
            return {"status": "error", "message": f"Brak danych w 'value' dla {date}"}

        # Grupowanie danych 15-minutowych do pełnych godzin
        hourly_map = {} # klucz: godzina, wartość: lista cen
        
        for item in values:
            hour = int(item['dtime'].split(" ")[1].split(":")[0])
            price = float(item['rce_pln'])
            
            if hour not in hourly_map:
                hourly_map[hour] = []
            hourly_map[hour].append(price)

        # Tworzenie listy 24-godzinnej (średnia z każdej godziny)
        result = []
        for h in range(24):
            if h in hourly_map:
                avg_price = sum(hourly_map[h]) / len(hourly_map[h])
                result.append({
                    "hour": h,
                    "price": round(avg_price, 2)
                })
        
        return result

    except Exception as e:
        return {"status": "error", "message": str(e)}

class ScheduleRequest(BaseModel):
    machineId: int
    plannedHour: int

@app.post("/machine/schedule")
async def receive_schedule(request: ScheduleRequest):
    print(f"--- OTRZYMANO ZLECENIE ---")
    print(f"Maszyna ID: {request.machineId}")
    print(f"Planowany start: {request.plannedHour}:00")
    return {"status": "success", "message": "Plan received"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
