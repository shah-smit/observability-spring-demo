def register(params)
    @customer_cin = params["customer_cin"]
    @customer_suffix = params["customer_suffix"]
    @format_identifier = params["format_identifier"]
end

def filter(event)
    if event.get('message').include?("GET Greeting")

        if !event.get('dob').nil?
            # dob == 9 Characters
            dob = event.get("dob")
            puts dob
            dob = dob + "         "
            dob = dob[0, 9]
            event.set("dob_updated",dob)
        end
    end
    return [event]
end