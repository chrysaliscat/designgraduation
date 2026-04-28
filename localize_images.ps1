# Create directory
$dir = "g:\SYSTEM_DESIGN\RuoYi-Vue\ruoyi-ui\public\products"
if (!(Test-Path -Path $dir)) {
    New-Item -ItemType Directory -Path $dir | Out-Null
    Write-Host "Created directory: $dir"
}

# Define images (Name -> URL)
$images = @{
    "tomato.jpg" = "https://images.pexels.com/photos/1327838/pexels-photo-1327838.jpeg?auto=compress&cs=tinysrgb&w=800"
    "apple.jpg" = "https://images.pexels.com/photos/102104/pexels-photo-102104.jpeg?auto=compress&cs=tinysrgb&w=800"
    "rice.jpg" = "https://images.pexels.com/photos/4110251/pexels-photo-4110251.jpeg?auto=compress&cs=tinysrgb&w=800"
    "eggs.jpg" = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Chicken_egg_2009-06-04.jpg/640px-Chicken_egg_2009-06-04.jpg"
    "corn.jpg" = "https://images.pexels.com/photos/547263/pexels-photo-547263.jpeg?auto=compress&cs=tinysrgb&w=800"
    "tea.jpg" = "https://images.pexels.com/photos/227908/pexels-photo-227908.jpeg?auto=compress&cs=tinysrgb&w=800"
    "strawberry.jpg" = "https://images.pexels.com/photos/46174/strawberries-berries-fruit-freshness-46174.jpeg?auto=compress&cs=tinysrgb&w=800"
    "cabbage.jpg" = "https://images.pexels.com/photos/2518893/pexels-photo-2518893.jpeg?auto=compress&cs=tinysrgb&w=800"
    "cucumber.jpg" = "https://images.pexels.com/photos/2329440/pexels-photo-2329440.jpeg?auto=compress&cs=tinysrgb&w=800"
    "chicken.jpg" = "https://images.pexels.com/photos/1769279/pexels-photo-1769279.jpeg?auto=compress&cs=tinysrgb&w=800"
    "pork.jpg" = "https://images.pexels.com/photos/65175/pexels-photo-65175.jpeg?auto=compress&cs=tinysrgb&w=800"
}

# Download loop
foreach ($name in $images.Keys) {
    $url = $images[$name]
    $output = Join-Path -Path $dir -ChildPath $name
    Write-Host "Downloading $name ..."
    try {
        Invoke-WebRequest -Uri $url -OutFile $output
        Write-Host "Success: $name"
    } catch {
        Write-Host "Failed to download $name : $_"
    }
}
