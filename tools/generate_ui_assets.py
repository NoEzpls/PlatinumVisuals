"""Generate original antialiased GUI sprites; Pillow is only needed to regenerate assets."""
from PIL import Image,ImageDraw,ImageFilter
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/platinumvisuals/textures/gui/sprites/ui'
ROOT.mkdir(parents=True,exist_ok=True)
S=8
def save(im,name):im.resize((im.width//S,im.height//S),Image.Resampling.LANCZOS).save(ROOT/(name+'.png'))
def rounded(name,size,r,border):
 im=Image.new('RGBA',(size*S,size*S));d=ImageDraw.Draw(im);d.rounded_rectangle((0,0,size*S-1,size*S-1),r*S,fill='white');save(im,name)
 (ROOT/(name+'.png.mcmeta')).write_text(json.dumps({'gui':{'scaling':{'type':'nine_slice','width':size,'height':size,'border':border,'stretch_inner':True}}}))
rounded('round',32,8,8);rounded('pill',16,7,7)
im=Image.new('RGBA',(64*S,64*S));d=ImageDraw.Draw(im);d.rounded_rectangle((16*S,16*S,48*S,48*S),8*S,fill='white');im=im.filter(ImageFilter.GaussianBlur(6*S));save(im,'shadow')
(ROOT/'shadow.png.mcmeta').write_text(json.dumps({'gui':{'scaling':{'type':'nine_slice','width':64,'height':64,'border':24,'stretch_inner':True}}}))
def icon(name,lines=(),circles=(),rects=()):
 im=Image.new('RGBA',(24*S,24*S));d=ImageDraw.Draw(im)
 for pts in lines:d.line([(x*S,y*S) for x,y in pts],fill='white',width=2*S,joint='curve')
 for x,y,r in circles:d.ellipse(((x-r)*S,(y-r)*S,(x+r)*S,(y+r)*S),outline='white',width=2*S)
 for x,y,w,h in rects:d.rounded_rectangle((x*S,y*S,(x+w)*S,(y+h)*S),2*S,outline='white',width=2*S)
 save(im,name)
icon('visuals',[[(2,12),(6,7),(12,5),(18,7),(22,12),(18,17),(12,19),(6,17),(2,12)]],[(12,12,3)])
icon('functions',[[(14,2),(5,13),(11,13),(9,22),(19,10),(13,10),(14,2)]])
icon('hud',rects=[(3,3,7,7),(14,3,7,7),(3,14,7,7),(14,14,7,7)])
icon('profile',[[(4,3),(18,3),(21,6),(21,21),(3,21),(3,3),(4,3)],[(7,3),(7,9),(16,9),(16,3)],[(7,21),(7,14),(17,14),(17,21)]])
icon('search',[[(16,16),(21,21)]],[(10,10,7)])
icon('close',[[(6,6),(18,18)],[(6,18),(18,6)]])
icon('arrow',[[(9,5),(16,12),(9,19)]])
icon('tune',[[(3,6),(21,6)],[(3,12),(21,12)],[(3,18),(21,18)]],[(8,6,2),(16,12,2),(10,18,2)])
icon('logo',[[(5,21),(5,3),(14,3),(19,8),(14,13),(5,13)],[(9,17),(18,17)]])
icon('check',[[(5,12),(10,17),(20,6)]])
print('Generated',len(list(ROOT.glob('*.png'))),'original sprites')
